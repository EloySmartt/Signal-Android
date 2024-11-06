package com.smarttmessenger.app.components.settings.app.subscription.receipts.detail

import com.smarttmessenger.app.database.model.InAppPaymentReceiptRecord

data class DonationReceiptDetailState(
  val inAppPaymentReceiptRecord: InAppPaymentReceiptRecord? = null,
  val subscriptionName: String? = null
)
