package com.smarttmessenger.app.migrations

import com.smarttmessenger.app.dependencies.AppDependencies
import com.smarttmessenger.app.jobmanager.Job
import com.smarttmessenger.app.jobs.MultiDeviceKeysUpdateJob
import com.smarttmessenger.app.util.TextSecurePreferences

/**
 * Migration to sync keys with linked devices.
 */
internal class SyncKeysMigrationJob(
  parameters: Parameters = Parameters.Builder().build()
) : MigrationJob(parameters) {

  companion object {
    const val KEY = "SyncKeysMigrationJob"
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    if (TextSecurePreferences.isMultiDevice(context)) {
      AppDependencies.jobManager.add(MultiDeviceKeysUpdateJob())
    }
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<SyncKeysMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): SyncKeysMigrationJob {
      return SyncKeysMigrationJob(parameters)
    }
  }
}
