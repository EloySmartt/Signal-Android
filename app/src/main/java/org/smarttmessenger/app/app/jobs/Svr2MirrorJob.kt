/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package com.smarttmessenger.app.jobs

import org.signal.core.util.logging.Log
import com.smarttmessenger.app.BuildConfig
import com.smarttmessenger.app.dependencies.AppDependencies
import com.smarttmessenger.app.jobmanager.Job
import com.smarttmessenger.app.jobmanager.JsonJobData
import com.smarttmessenger.app.jobmanager.impl.NetworkConstraint
import com.smarttmessenger.app.keyvalue.SignalStore
import com.smarttmessenger.app.pin.Svr3Migration
import com.smarttmessenger.app.pin.SvrRepository
import org.whispersystems.signalservice.api.push.exceptions.NonSuccessfulResponseCodeException
import org.whispersystems.signalservice.api.svr.SecureValueRecovery.BackupResponse
import org.whispersystems.signalservice.api.svr.SecureValueRecovery.PinChangeSession
import org.whispersystems.signalservice.api.svr.SecureValueRecoveryV2
import kotlin.concurrent.withLock
import kotlin.time.Duration.Companion.days

/**
 * Ensures a user's SVR data is written to SVR2.
 */
class Svr2MirrorJob private constructor(parameters: Parameters, private var serializedChangeSession: String?) : Job(parameters) {

  companion object {
    const val KEY = "Svr2MirrorJob"

    private val TAG = Log.tag(Svr2MirrorJob::class.java)

    private const val KEY_CHANGE_SESSION = "change_session"
  }

  constructor() : this(
    Parameters.Builder()
      .addConstraint(NetworkConstraint.KEY)
      .setLifespan(30.days.inWholeMilliseconds)
      .setMaxAttempts(Parameters.UNLIMITED)
      .setQueue("Svr2MirrorJob")
      .setMaxInstancesForFactory(1)
      .build(),
    null
  )

  override fun serialize(): ByteArray? {
    return JsonJobData.Builder()
      .putString(KEY_CHANGE_SESSION, serializedChangeSession)
      .build()
      .serialize()
  }

  override fun getFactoryKey(): String = KEY

  override fun run(): Result {
    if (!Svr3Migration.shouldWriteToSvr2) {
      Log.w(TAG, "Writes to SVR2 are disabled. Skipping.")
      return Result.success()
    }

    SvrRepository.operationLock.withLock {
      val pin = SignalStore.svr.pin

      if (SignalStore.svr.hasOptedOut()) {
        Log.w(TAG, "Opted out of SVR! Nothing to migrate.")
        return Result.success()
      }

      if (pin == null) {
        Log.w(TAG, "No PIN available! Can't migrate!")
        return Result.success()
      }

      val svr2: SecureValueRecoveryV2 = AppDependencies.signalServiceAccountManager.getSecureValueRecoveryV2(BuildConfig.SVR2_MRENCLAVE)

      val session: PinChangeSession = serializedChangeSession?.let { session ->
        svr2.resumePinChangeSession(pin, SignalStore.svr.getOrCreateMasterKey(), session)
      } ?: svr2.setPin(pin, SignalStore.svr.getOrCreateMasterKey())

      serializedChangeSession = session.serialize()

      return when (val response: BackupResponse = session.execute()) {
        is BackupResponse.Success -> {
          Log.i(TAG, "Successfully migrated to SVR2! $svr2")
          SignalStore.svr.appendSvr2AuthTokenToList(response.authorization.asBasic())
          AppDependencies.jobManager.add(RefreshAttributesJob())
          Result.success()
        }
        is BackupResponse.ApplicationError -> {
          if (response.exception.isUnauthorized()) {
            Log.w(TAG, "Unauthorized! Giving up.", response.exception)
            Result.success()
          } else {
            Log.w(TAG, "Hit an application error. Retrying.", response.exception)
            Result.retry(defaultBackoff())
          }
        }
        BackupResponse.EnclaveNotFound -> {
          Log.w(TAG, "Could not find the enclave. Giving up.")
          Result.success()
        }
        BackupResponse.ExposeFailure -> {
          Log.w(TAG, "Failed to expose the backup. Giving up.")
          Result.success()
        }
        is BackupResponse.NetworkError -> {
          Log.w(TAG, "Hit a network error. Retrying.", response.exception)
          Result.retry(defaultBackoff())
        }
        BackupResponse.ServerRejected -> {
          Log.w(TAG, "Server told us to stop trying. Giving up.")
          Result.success()
        }
      }
    }
  }

  private fun Throwable.isUnauthorized(): Boolean {
    return this is NonSuccessfulResponseCodeException && this.code == 401
  }

  override fun onFailure() = Unit

  class Factory : Job.Factory<Svr2MirrorJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): Svr2MirrorJob {
      return Svr2MirrorJob(parameters, JsonJobData.deserialize(serializedData).getString(KEY_CHANGE_SESSION))
    }
  }
}
