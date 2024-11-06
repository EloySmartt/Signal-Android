/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.calls.links

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.ringrtc.CallLinkState
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.dependencies.AppDependencies
import com.smarttmessenger.app.jobs.CallLinkUpdateSendJob
import com.smarttmessenger.app.service.webrtc.links.CallLinkCredentials
import com.smarttmessenger.app.service.webrtc.links.SignalCallLinkManager
import com.smarttmessenger.app.service.webrtc.links.UpdateCallLinkResult
import com.smarttmessenger.app.storage.StorageSyncHelper

/**
 * Repository for performing update operations on call links:
 * <ul>
 *   <li>Set name</li>
 *   <li>Set restrictions</li>
 *   <li>Revoke link</li>
 * </ul>
 *
 * All of these will delegate to the [SignalCallLinkManager] but will additionally update the database state.
 */
class UpdateCallLinkRepository(
  private val callLinkManager: SignalCallLinkManager = AppDependencies.signalCallManager.callLinkManager
) {
  fun setCallName(credentials: CallLinkCredentials, name: String): Single<UpdateCallLinkResult> {
    return callLinkManager
      .updateCallLinkName(
        credentials = credentials,
        name = name
      )
      .doOnSuccess(updateState(credentials))
      .subscribeOn(Schedulers.io())
  }

  fun setCallRestrictions(credentials: CallLinkCredentials, restrictions: CallLinkState.Restrictions): Single<UpdateCallLinkResult> {
    return callLinkManager
      .updateCallLinkRestrictions(
        credentials = credentials,
        restrictions = restrictions
      )
      .doOnSuccess(updateState(credentials))
      .subscribeOn(Schedulers.io())
  }

  fun deleteCallLink(credentials: CallLinkCredentials): Single<UpdateCallLinkResult> {
    return callLinkManager
      .deleteCallLink(credentials)
      .doOnSuccess(updateState(credentials))
      .subscribeOn(Schedulers.io())
  }

  private fun updateState(credentials: CallLinkCredentials): (UpdateCallLinkResult) -> Unit {
    return { result ->
      when (result) {
        is UpdateCallLinkResult.Update -> {
          SignalDatabase.callLinks.updateCallLinkState(credentials.roomId, result.state)
          AppDependencies.jobManager.add(CallLinkUpdateSendJob(credentials.roomId))
        }
        is UpdateCallLinkResult.Delete -> {
          SignalDatabase.callLinks.markRevoked(credentials.roomId)
          AppDependencies.jobManager.add(CallLinkUpdateSendJob(credentials.roomId))
          StorageSyncHelper.scheduleSyncForDataChange()
        }
        else -> {}
      }
    }
  }
}
