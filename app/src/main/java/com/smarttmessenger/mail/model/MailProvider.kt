package com.smarttmessenger.mail.model

/** Supported mail providers. Real auth differs per provider (see package README). */
enum class MailProvider(val displayName: String) {
  GMAIL("Gmail"),
  OUTLOOK("Outlook")
}
