package com.smarttmessenger.app.components.settings.app.subscription.receipts.list

import com.smarttmessenger.app.badges.models.Badge
import com.smarttmessenger.app.database.model.InAppPaymentReceiptRecord

data class DonationReceiptBadge(
  val type: InAppPaymentReceiptRecord.Type,
  val level: Int,
  val badge: Badge
)
