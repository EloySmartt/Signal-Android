/**
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.calls.links.create

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.ringrtc.CallLinkState.Restrictions
import com.smarttmessenger.app.calls.links.CallLinks
import com.smarttmessenger.app.calls.links.UpdateCallLinkRepository
import com.smarttmessenger.app.database.CallLinkTable
import com.smarttmessenger.app.recipients.RecipientId
import com.smarttmessenger.app.service.webrtc.links.CallLinkCredentials
import com.smarttmessenger.app.service.webrtc.links.SignalCallLinkState
import com.smarttmessenger.app.service.webrtc.links.UpdateCallLinkResult
import java.time.Instant

class CreateCallLinkViewModel(
  private val repository: CreateCallLinkRepository = CreateCallLinkRepository(),
  private val mutationRepository: UpdateCallLinkRepository = UpdateCallLinkRepository()
) : ViewModel() {
  private val credentials = CallLinkCredentials.generate()
  private val _callLink: MutableState<CallLinkTable.CallLink> = mutableStateOf(
    CallLinkTable.CallLink(
      recipientId = RecipientId.UNKNOWN,
      roomId = credentials.roomId,
      credentials = credentials,
      state = SignalCallLinkState(
        name = "",
        restrictions = Restrictions.ADMIN_APPROVAL,
        revoked = false,
        expiration = Instant.MAX
      ),
      deletionTimestamp = 0L
    )
  )

  val callLink: State<CallLinkTable.CallLink> = _callLink
  val linkKeyBytes: ByteArray = credentials.linkKeyBytes

  private val disposables = CompositeDisposable()

  init {
    disposables += CallLinks.watchCallLink(credentials.roomId)
      .subscribeBy {
        _callLink.value = it
      }
  }

  override fun onCleared() {
    super.onCleared()
    disposables.dispose()
  }

  fun commitCallLink(): Single<EnsureCallLinkCreatedResult> {
    return repository.ensureCallLinkCreated(credentials)
      .observeOn(AndroidSchedulers.mainThread())
  }

  fun setApproveAllMembers(approveAllMembers: Boolean): Single<UpdateCallLinkResult> {
    return commitCallLink()
      .flatMap {
        when (it) {
          is EnsureCallLinkCreatedResult.Success -> mutationRepository.setCallRestrictions(
            credentials,
            if (approveAllMembers) Restrictions.ADMIN_APPROVAL else Restrictions.NONE
          )
          is EnsureCallLinkCreatedResult.Failure -> Single.just(UpdateCallLinkResult.Failure(it.failure.status))
        }
      }
      .observeOn(AndroidSchedulers.mainThread())
  }

  fun toggleApproveAllMembers(): Single<UpdateCallLinkResult> {
    return setApproveAllMembers(_callLink.value.state.restrictions != Restrictions.ADMIN_APPROVAL)
      .observeOn(AndroidSchedulers.mainThread())
  }

  fun setCallName(callName: String): Single<UpdateCallLinkResult> {
    return commitCallLink()
      .flatMap {
        when (it) {
          is EnsureCallLinkCreatedResult.Success -> mutationRepository.setCallName(
            credentials,
            callName
          )
          is EnsureCallLinkCreatedResult.Failure -> Single.just(UpdateCallLinkResult.Failure(it.failure.status))
        }
      }
      .observeOn(AndroidSchedulers.mainThread())
  }
}
