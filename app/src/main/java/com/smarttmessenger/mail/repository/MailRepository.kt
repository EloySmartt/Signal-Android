package com.smarttmessenger.mail.repository

import com.smarttmessenger.mail.model.MailAccount
import com.smarttmessenger.mail.model.MailMessage
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.logging.Log
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Single source of truth for mail, modeled on CommunicationWindowsRepository (local-first, RxJava3).
 *
 * SCAFFOLD: reads/writes are backed by in-memory lists so the UI can be built and demoed now.
 * To finish (see package README):
 *   - Replace the in-memory stores with a local table (mirror SmarttCommunicationWindowsTable /
 *     SmarttWindowHeldTable) + an observer (mirror SmarttWindowsObserver) for reactive reads.
 *   - Implement [sync] with a real provider fetch (Gmail API / Microsoft Graph), authenticated with
 *     Android OAuth (Google Sign-In/AppAuth, MSAL Android).
 *
 * Held/deliver mirrors the communication-window held state: fetched mail is stored with held=true
 * and flipped to held=false (delivered) when the account's delivery window opens.
 */
class MailRepository {

  private val accounts = CopyOnWriteArrayList<MailAccount>()
  private val messages = CopyOnWriteArrayList<MailMessage>()
  private val listeners = CopyOnWriteArrayList<() -> Unit>()

  private fun notifyChanged() = listeners.forEach { it() }

  private fun <T> reactive(read: () -> T): Flowable<T> =
    Observable.create { emitter: ObservableEmitter<T> ->
      val listener = { emitter.onNext(read()) }
      listeners.add(listener)
      emitter.setCancellable { listeners.remove(listener) }
      listener()
    }.subscribeOn(Schedulers.io()).toFlowable(BackpressureStrategy.LATEST)

  fun getAccounts(): Flowable<List<MailAccount>> = reactive { accounts.toList() }

  fun getMessages(): Flowable<List<MailMessage>> = reactive { messages.toList() }

  fun getLocalMessagesSync(): List<MailMessage> = messages.toList()

  /** Connect (or replace) the account. Local-first: returns immediately. */
  fun connect(account: MailAccount): Single<MailAccount> = Single.fromCallable {
    accounts.clear()
    accounts.add(account)
    notifyChanged()
    account
  }.subscribeOn(Schedulers.io())

  fun disconnect(): Completable = Completable.fromAction {
    accounts.clear()
    messages.clear()
    notifyChanged()
  }.subscribeOn(Schedulers.io())

  /** TODO: real provider fetch. New messages are stored held=true. */
  fun sync(): Completable = Completable.fromAction {
    Log.i(TAG, "sync(): scaffold no-op — wire Gmail/Graph fetch here")
    notifyChanged()
  }.subscribeOn(Schedulers.io())

  /** Reveal every held message (window opened, or manual "Deliver now"). */
  fun deliverHeld(): Completable = Completable.fromAction {
    var changed = false
    for (i in messages.indices) {
      if (messages[i].held) {
        messages[i] = messages[i].copy(held = false)
        changed = true
      }
    }
    if (changed) notifyChanged()
  }.subscribeOn(Schedulers.io())

  companion object {
    private val TAG = Log.tag(MailRepository::class.java)
  }
}
