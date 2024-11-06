package com.smarttmessenger.app.database.model

import com.smarttmessenger.app.recipients.RecipientId

/** A model for [com.smarttmessenger.app.database.PendingRetryReceiptTable] */
data class PendingRetryReceiptModel(
  val id: Long,
  val author: RecipientId,
  val authorDevice: Int,
  val sentTimestamp: Long,
  val receivedTimestamp: Long,
  val threadId: Long
)
