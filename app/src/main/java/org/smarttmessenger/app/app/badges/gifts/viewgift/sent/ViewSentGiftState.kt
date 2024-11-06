package com.smarttmessenger.app.badges.gifts.viewgift.sent

import com.smarttmessenger.app.badges.models.Badge
import com.smarttmessenger.app.recipients.Recipient

data class ViewSentGiftState(
  val recipient: Recipient? = null,
  val badge: Badge? = null
)
