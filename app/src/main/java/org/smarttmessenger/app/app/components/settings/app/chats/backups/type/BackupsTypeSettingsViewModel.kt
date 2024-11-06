/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.components.settings.app.chats.backups.type

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.smarttmessenger.app.backup.v2.BackupRepository
import com.smarttmessenger.app.components.settings.app.subscription.InAppPaymentsRepository
import com.smarttmessenger.app.components.settings.app.subscription.InAppPaymentsRepository.toPaymentSourceType
import com.smarttmessenger.app.database.model.InAppPaymentSubscriberRecord
import com.smarttmessenger.app.keyvalue.SignalStore

class BackupsTypeSettingsViewModel : ViewModel() {
  private val internalState = MutableStateFlow(BackupsTypeSettingsState())

  val state: StateFlow<BackupsTypeSettingsState> = internalState

  init {
    refresh()
  }

  fun refresh() {
    viewModelScope.launch {
      val tier = SignalStore.backup.backupTier
      val paymentMethod = withContext(Dispatchers.IO) {
        InAppPaymentsRepository.getLatestPaymentMethodType(InAppPaymentSubscriberRecord.Type.BACKUP)
      }

      internalState.update {
        it.copy(
          messageBackupsType = if (tier != null) BackupRepository.getBackupsType(tier) else null,
          paymentSourceType = paymentMethod.toPaymentSourceType()
        )
      }
    }
  }
}
