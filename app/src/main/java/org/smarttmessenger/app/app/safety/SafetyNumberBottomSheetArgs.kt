package com.smarttmessenger.app.safety

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.smarttmessenger.app.contacts.paged.ContactSearchKey
import com.smarttmessenger.app.database.model.MessageId
import com.smarttmessenger.app.recipients.RecipientId

/**
 * Fragment argument for `SafetyNumberBottomSheetFragment`
 */
@Parcelize
data class SafetyNumberBottomSheetArgs(
  val untrustedRecipients: List<RecipientId>,
  val destinations: List<ContactSearchKey.RecipientSearchKey>,
  val messageId: MessageId? = null
) : Parcelable
