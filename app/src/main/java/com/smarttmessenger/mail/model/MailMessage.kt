package com.smarttmessenger.mail.model

/** A fetched email, normalized across providers (Gmail API / Microsoft Graph). */
data class MailMessage(
  val messageId: String = "",
  val accountId: String = "",
  val fromName: String = "",
  val fromAddress: String = "",
  val subject: String = "",
  val preview: String = "",
  val body: String = "",
  val receivedAtMs: Long = 0L,
  val unread: Boolean = true,
  val starred: Boolean = false,
  /** True while fetched but held (window closed); false once delivered/visible. */
  val held: Boolean = true
)
