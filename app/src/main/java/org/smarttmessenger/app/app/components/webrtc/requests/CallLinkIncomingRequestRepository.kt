/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package com.smarttmessenger.app.components.webrtc.requests

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import com.smarttmessenger.app.contacts.paged.GroupsInCommon
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.recipients.Recipient
import com.smarttmessenger.app.recipients.RecipientId

class CallLinkIncomingRequestRepository {

  fun getGroupsInCommon(recipientId: RecipientId): Observable<GroupsInCommon> {
    return Recipient.observable(recipientId).flatMapSingle { recipient ->
      if (recipient.hasGroupsInCommon) {
        Single.fromCallable {
          val groupsInCommon = SignalDatabase.groups.getGroupsContainingMember(recipient.id, true)
          val total = groupsInCommon.size
          val names = groupsInCommon.take(2).map { it.title!! }
          GroupsInCommon(total, names)
        }.observeOn(Schedulers.io())
      } else {
        Single.just(GroupsInCommon(0, listOf()))
      }
    }
  }
}
