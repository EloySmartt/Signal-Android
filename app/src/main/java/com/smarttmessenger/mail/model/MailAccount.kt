package com.smarttmessenger.mail.model

/**
 * A connected mail account. Client owns [accountId] (like CommunicationWindow.windowId), so the same
 * id works for create + edit and for local-first upserts.
 */
data class MailAccount(
  val accountId: String = "",
  val provider: MailProvider = MailProvider.GMAIL,
  val emailAddress: String = "",
  val displayName: String = "",
  val connectedAtMs: Long = 0L,
  val deliveryWindow: MailDeliveryWindow = MailDeliveryWindow()
)
