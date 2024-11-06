package com.smarttmessenger.app.conversation.mutiselect.forward

import com.smarttmessenger.app.contacts.paged.ContactSearchKey
import com.smarttmessenger.app.database.model.IdentityRecord
import com.smarttmessenger.app.stories.Stories

data class MultiselectForwardState(
  val stage: Stage = Stage.Selection,
  val storySendRequirements: Stories.MediaTransform.SendRequirements = Stories.MediaTransform.SendRequirements.CAN_NOT_SEND
) {

  sealed class Stage {
    object Selection : Stage()
    object FirstConfirmation : Stage()
    object LoadingIdentities : Stage()
    data class SafetyConfirmation(val identities: List<IdentityRecord>, val selectedContacts: List<ContactSearchKey>) : Stage()
    object SendPending : Stage()
    object SomeFailed : Stage()
    object AllFailed : Stage()
    object Success : Stage()
    data class SelectionConfirmed(val selectedContacts: Set<ContactSearchKey>) : Stage()
  }
}
