package com.smarttmessenger.communicationwindow.repository

import android.content.Context
import com.smarttmessenger.communicationwindow.database.SmarttCommunicationWindowsTable
import com.smarttmessenger.communicationwindow.database.SmarttWindowsObserver
import com.smarttmessenger.communicationwindow.model.CommunicationWindow
import com.smarttmessenger.communicationwindow.network.CommunicationWindowRequest
import com.smarttmessenger.communicationwindow.network.SmarttCommunicationWindowApi
import com.smarttmessenger.communicationwindow.network.WindowMetadataResponse
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.dependencies.AppDependencies

/**
 * Single source of truth for communication-window data, modeled on NotificationProfilesRepository:
 * **local-first**. Reads come reactively from the local table (via [SmarttWindowsObserver]); writes
 * hit the local table immediately and are pushed to our server in the background (the equivalent of
 * NP's storage-service sync, but against our own Smartt REST API since the server enforces windows).
 *
 * This is why save+navigate is instant and never hangs when the server is unreachable — the UI never
 * blocks on the network. See [[project-communication-window-architecture]].
 */
class CommunicationWindowsRepository(private val context: Context) {

  private val localTable = SmarttCommunicationWindowsTable(context)
  private val api: SmarttCommunicationWindowApi by lazy {
    SmarttCommunicationWindowApi(AppDependencies.signalOkHttpClient)
  }

  // --- Reactive reads (mirror NotificationProfilesRepository.getProfiles/getProfile) ---

  fun getWindows(): Flowable<List<CommunicationWindow>> {
    return Observable.create { emitter: ObservableEmitter<List<CommunicationWindow>> ->
      val listener = { emitter.onNext(localTable.getAll()) }
      SmarttWindowsObserver.register(listener)
      emitter.setCancellable { SmarttWindowsObserver.unregister(listener) }
      listener()
    }.subscribeOn(Schedulers.io()).toFlowable(BackpressureStrategy.LATEST)
  }

  fun getWindow(windowId: String): Observable<CommunicationWindow> {
    return Observable.create { emitter: ObservableEmitter<CommunicationWindow> ->
      val listener = {
        val window = localTable.get(windowId)
        if (window != null) emitter.onNext(window) else emitter.onError(NoSuchWindowException(windowId))
      }
      SmarttWindowsObserver.register(listener)
      emitter.setCancellable { SmarttWindowsObserver.unregister(listener) }
      listener()
    }.subscribeOn(Schedulers.io())
  }

  /** Synchronous local read. Caller must be off the main thread. */
  fun getLocalWindowsSync(): List<CommunicationWindow> = localTable.getAll()

  // --- Local-first writes ---

  /** Create or update (upsert). Writes locally and returns immediately; pushes to the server in the
   *  background. The client owns the windowId, so this works for both create and edit. */
  fun save(window: CommunicationWindow): Single<CommunicationWindow> {
    return Single.fromCallable {
      localTable.upsert(window, needsSync = true)
      pushToServer(window)
      window
    }.subscribeOn(Schedulers.io())
  }

  fun delete(windowId: String): Completable {
    return Completable.fromAction {
      localTable.delete(windowId)
      SignalExecutors.BOUNDED_IO.execute {
        runCatching { api.deleteWindow(windowId) }
          .onFailure { Log.w(TAG, "Background delete failed for $windowId; will not retry", it) }
      }
    }.subscribeOn(Schedulers.io())
  }

  /** Fire-and-forget push of a single window; clears the needs-sync flag on success. */
  private fun pushToServer(window: CommunicationWindow) {
    SignalExecutors.BOUNDED_IO.execute {
      runCatching {
        api.updateWindow(window.windowId, CommunicationWindowRequest.from(window))
        localTable.markSynced(window.windowId)
      }.onFailure { Log.w(TAG, "Background sync failed for ${window.windowId}; will retry on next syncPending()", it) }
    }
  }

  /** Retry every locally-changed-but-unpushed window. Call at startup / on reconnect. */
  fun syncPending() {
    SignalExecutors.BOUNDED_IO.execute {
      localTable.getPendingSync().forEach { window ->
        runCatching {
          api.updateWindow(window.windowId, CommunicationWindowRequest.from(window))
          localTable.markSynced(window.windowId)
        }.onFailure { Log.w(TAG, "syncPending failed for ${window.windowId}", it) }
      }
    }
  }

  /** Pull all windows from the server into the local table (e.g. fresh install / multi-device). */
  fun syncFromServer(): Completable {
    return Completable.fromAction {
      api.getWindows().map { it.toModel() }.forEach { localTable.upsert(it, needsSync = false) }
    }.subscribeOn(Schedulers.io())
  }

  fun getWindowMetadata(recipientAci: String): Single<WindowMetadataResponse> {
    return Single.fromCallable { api.getWindowMetadata(recipientAci) }.subscribeOn(Schedulers.io())
  }

  class NoSuchWindowException(id: String) : Exception("No window with id $id")

  companion object {
    private val TAG = Log.tag(CommunicationWindowsRepository::class.java)
  }
}
