/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.badges.self.expired

import androidx.annotation.StringRes
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.signal.donations.InAppPaymentType
import org.signal.donations.StripeDeclineCode
import org.signal.donations.StripeFailureCode
import com.smarttmessenger.app.badges.Badges
import com.smarttmessenger.app.components.settings.app.subscription.InAppPaymentsRepository.toActiveSubscriptionChargeFailure
import com.smarttmessenger.app.components.settings.app.subscription.errors.mapToErrorStringResource
import com.smarttmessenger.app.database.InAppPaymentTable
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.keyvalue.SignalStore
import org.whispersystems.signalservice.api.subscriptions.ActiveSubscription.ChargeFailure

class MonthlyDonationCanceledViewModel(
  inAppPaymentId: InAppPaymentTable.InAppPaymentId?
) : ViewModel() {
  private val internalState = mutableStateOf(MonthlyDonationCanceledState())
  val state: State<MonthlyDonationCanceledState> = internalState

  init {
    if (inAppPaymentId != null) {
      initializeFromInAppPaymentId(inAppPaymentId)
    } else {
      initializeFromSignalStore()
    }
  }

  private fun initializeFromInAppPaymentId(inAppPaymentId: InAppPaymentTable.InAppPaymentId) {
    viewModelScope.launch {
      val inAppPayment = withContext(Dispatchers.IO) {
        SignalDatabase.inAppPayments.getById(inAppPaymentId)
      }

      if (inAppPayment != null) {
        internalState.value = MonthlyDonationCanceledState(
          loadState = MonthlyDonationCanceledState.LoadState.READY,
          badge = Badges.fromDatabaseBadge(inAppPayment.data.badge!!),
          errorMessage = getErrorMessage(inAppPayment.data.cancellation?.chargeFailure?.toActiveSubscriptionChargeFailure())
        )
      } else {
        internalState.value = internalState.value.copy(loadState = MonthlyDonationCanceledState.LoadState.FAILED)
      }
    }
  }

  private fun initializeFromSignalStore() {
    internalState.value = MonthlyDonationCanceledState(
      loadState = MonthlyDonationCanceledState.LoadState.READY,
      badge = SignalStore.inAppPayments.getExpiredBadge(),
      errorMessage = getErrorMessage(SignalStore.inAppPayments.getUnexpectedSubscriptionCancelationChargeFailure())
    )
  }

  @StringRes
  private fun getErrorMessage(chargeFailure: ChargeFailure?): Int {
    val declineCode: StripeDeclineCode = StripeDeclineCode.getFromCode(chargeFailure?.outcomeNetworkReason)
    val failureCode: StripeFailureCode = StripeFailureCode.getFromCode(chargeFailure?.code)

    return if (declineCode.isKnown()) {
      declineCode.mapToErrorStringResource()
    } else if (failureCode.isKnown) {
      failureCode.mapToErrorStringResource(InAppPaymentType.RECURRING_DONATION)
    } else {
      declineCode.mapToErrorStringResource()
    }
  }
}
