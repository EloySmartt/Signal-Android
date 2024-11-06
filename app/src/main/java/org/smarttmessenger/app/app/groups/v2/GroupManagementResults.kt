package com.smarttmessenger.app.groups.v2

import com.smarttmessenger.app.groups.ui.GroupChangeFailureReason
import com.smarttmessenger.app.recipients.Recipient

sealed class GroupBlockJoinRequestResult {
  object Success : GroupBlockJoinRequestResult()
  class Failure(val reason: GroupChangeFailureReason) : GroupBlockJoinRequestResult()

  fun isFailure() = this is Failure
}

sealed class GroupAddMembersResult {
  class Success(val numberOfMembersAdded: Int, val newMembersInvited: List<Recipient>) : GroupAddMembersResult()
  class Failure(val reason: GroupChangeFailureReason) : GroupAddMembersResult()

  fun isFailure() = this is Failure
}
