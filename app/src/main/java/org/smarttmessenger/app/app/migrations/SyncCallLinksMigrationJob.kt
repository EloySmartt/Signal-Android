/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.migrations

import org.signal.core.util.logging.Log
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.jobmanager.Job
import com.smarttmessenger.app.keyvalue.SignalStore
import com.smarttmessenger.app.recipients.Recipient
import com.smarttmessenger.app.storage.StorageSyncHelper

/**
 * Marks all call links as needing to be synced by storage service.
 */
internal class SyncCallLinksMigrationJob @JvmOverloads constructor(parameters: Parameters = Parameters.Builder().build()) : MigrationJob(parameters) {

  companion object {
    const val KEY = "SyncCallLinksMigrationJob"

    private val TAG = Log.tag(SyncCallLinksMigrationJob::class)
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    if (SignalStore.account.aci == null) {
      Log.w(TAG, "Self not available yet.")
      return
    }

    val callLinkRecipients = SignalDatabase.callLinks.getAll().map { it.recipientId }.filter {
      try {
        Recipient.resolved(it)
        true
      } catch (e: Exception) {
        Log.e(TAG, "Unable to resolve recipient: $it")
        false
      }
    }

    SignalDatabase.recipients.markNeedsSync(callLinkRecipients)
    StorageSyncHelper.scheduleSyncForDataChange()
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<SyncCallLinksMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): SyncCallLinksMigrationJob {
      return SyncCallLinksMigrationJob(parameters)
    }
  }
}
