/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package com.smarttmessenger.app.components.settings.app.storage

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.smarttmessenger.app.backup.v2.BackupRepository
import com.smarttmessenger.app.backup.v2.MessageBackupTier
import com.smarttmessenger.app.backup.v2.ui.subscription.MessageBackupsType

class UpgradeToEnableOptimizedStorageViewModel : ViewModel() {
  private val internalMessageBackupsType = mutableStateOf<MessageBackupsType?>(null)
  val messageBackupsType: State<MessageBackupsType?> = internalMessageBackupsType

  init {
    viewModelScope.launch {
      val backupsType = withContext(Dispatchers.IO) {
        BackupRepository.getBackupsType(MessageBackupTier.PAID)
      }

      withContext(Dispatchers.Main) {
        internalMessageBackupsType.value = backupsType
      }
    }
  }
}
