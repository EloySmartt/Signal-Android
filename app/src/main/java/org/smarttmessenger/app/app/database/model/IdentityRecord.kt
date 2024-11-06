package com.smarttmessenger.app.database.model

import org.signal.libsignal.protocol.IdentityKey
import com.smarttmessenger.app.database.IdentityTable
import com.smarttmessenger.app.recipients.RecipientId

data class IdentityRecord(
  val recipientId: RecipientId,
  val identityKey: IdentityKey,
  val verifiedStatus: IdentityTable.VerifiedStatus,
  @get:JvmName("isFirstUse")
  val firstUse: Boolean,
  val timestamp: Long,
  @get:JvmName("isApprovedNonBlocking")
  val nonblockingApproval: Boolean
)
