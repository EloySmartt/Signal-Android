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
    SignalExecutors.BOUNDED_IO.execute { pushPendingBlocking() }
  }

  /**
   * Full startup sync: push local changes first, then pull the server's state down. The order
   * matters — pulling first would race the pending pushes. On a fresh install there is nothing to
   * push, so this is also what restores the user's windows after a reinstall or on a new device.
   */
  fun syncAll() {
    SignalExecutors.BOUNDED_IO.execute {
      pushPendingBlocking()
      runCatching { pullFromServerBlocking() }
        .onFailure { Log.w(TAG, "syncFromServer failed; local windows may be stale", it) }
    }
  }

  /** Caller must be off the main thread. */
  private fun pushPendingBlocking() {
    localTable.getPendingSync().forEach { window ->
      runCatching {
        api.updateWindow(window.windowId, CommunicationWindowRequest.from(window))
        localTable.markSynced(window.windowId)
      }.onFailure { Log.w(TAG, "syncPending failed for ${window.windowId}", it) }
    }
  }

  /**
   * Pull all windows from the server into the local table (e.g. fresh install / multi-device).
   *
   * Windows with unpushed local edits are skipped: the server copy is by definition older than what
   * [syncPending] is still trying to send, so overwriting them here would silently discard the
   * user's most recent change. They are left alone and reconciled once their push succeeds.
   *
   * Does not yet remove local windows the server no longer has — a window deleted on another device
   * lingers locally until it is deleted here too.
   */
  fun syncFromServer(): Completable {
    return Completable.fromAction { pullFromServerBlocking() }.subscribeOn(Schedulers.io())
  }

  /** Caller must be off the main thread. */
  private fun pullFromServerBlocking() {
    val pendingIds = localTable.getPendingSync().map { it.windowId }.toSet()
    api.getWindows()
      .map { it.toModel() }
      .filterNot { pendingIds.contains(it.windowId) }
      .forEach { localTable.upsert(it, needsSync = false) }
  }

  /** [recipientServiceId] is the wire form of an ACI (a bare UUID) or a PNI (`PNI:` + UUID). */
  fun getWindowMetadata(recipientServiceId: String): Single<WindowMetadataResponse> {
    return Single.fromCallable { api.getWindowMetadata(recipientServiceId) }.subscribeOn(Schedulers.io())
  }

  class NoSuchWindowException(id: String) : Exception("No window with id $id")

  companion object {
    private val TAG = Log.tag(CommunicationWindowsRepository::class.java)
  }
}
