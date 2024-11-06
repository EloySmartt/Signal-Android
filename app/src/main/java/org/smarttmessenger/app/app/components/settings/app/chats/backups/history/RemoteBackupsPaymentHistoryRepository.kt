/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.components.settings.app.chats.backups.history

import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.database.model.InAppPaymentReceiptRecord

object RemoteBackupsPaymentHistoryRepository {

  fun getReceipts(): List<InAppPaymentReceiptRecord> {
    return SignalDatabase.donationReceipts.getReceipts(InAppPaymentReceiptRecord.Type.RECURRING_BACKUP)
  }
}
