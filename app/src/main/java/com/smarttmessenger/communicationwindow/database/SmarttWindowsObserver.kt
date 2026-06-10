package com.smarttmessenger.communicationwindow.database

import java.util.concurrent.CopyOnWriteArraySet

/**
 * Lightweight change-notifier for the local communication-windows table — our own isolated
 * equivalent of Signal's DatabaseObserver (we deliberately do NOT hook Signal's observer, to keep
 * the feature mergeable). The table calls [notifyChanged] after every write; the repository
 * registers listeners so reads re-emit reactively, exactly like NotificationProfilesRepository.
 */
object SmarttWindowsObserver {

  private val listeners = CopyOnWriteArraySet<() -> Unit>()

  fun register(listener: () -> Unit) {
    listeners.add(listener)
  }

  fun unregister(listener: () -> Unit) {
    listeners.remove(listener)
  }

  fun notifyChanged() {
    listeners.forEach { it() }
  }
}
