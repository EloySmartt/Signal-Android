/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.backup.v2.ui.subscription

import com.smarttmessenger.app.backup.v2.MessageBackupTier
import com.smarttmessenger.app.database.InAppPaymentTable
import com.smarttmessenger.app.database.model.databaseprotos.InAppPaymentData
import com.smarttmessenger.app.keyvalue.SignalStore
import com.smarttmessenger.app.lock.v2.PinKeyboardType

data class MessageBackupsFlowState(
  val selectedMessageBackupTierLabel: String? = null,
  val selectedMessageBackupTier: MessageBackupTier? = SignalStore.backup.backupTier,
  val currentMessageBackupTier: MessageBackupTier? = SignalStore.backup.backupTier,
  val availableBackupTypes: List<MessageBackupsType> = emptyList(),
  val selectedPaymentMethod: InAppPaymentData.PaymentMethodType? = null,
  val availablePaymentMethods: List<InAppPaymentData.PaymentMethodType> = emptyList(),
  val pinKeyboardType: PinKeyboardType = SignalStore.pin.keyboardType,
  val inAppPayment: InAppPaymentTable.InAppPayment? = null,
  val startScreen: MessageBackupsScreen,
  val screen: MessageBackupsScreen = startScreen,
  val displayIncorrectPinError: Boolean = false
)
