package com.smarttmessenger.app.components.settings.app.subscription.receipts.list

import com.smarttmessenger.app.database.model.InAppPaymentReceiptRecord

data class DonationReceiptListPageState(
  val records: List<InAppPaymentReceiptRecord> = emptyList(),
  val isLoaded: Boolean = false
)
