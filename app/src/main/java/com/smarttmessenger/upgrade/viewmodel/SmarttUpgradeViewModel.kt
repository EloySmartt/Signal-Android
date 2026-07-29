/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.upgrade.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.smarttmessenger.upgrade.model.SmarttBillingPeriod
import com.smarttmessenger.upgrade.model.SmarttPlanId
import com.smarttmessenger.upgrade.model.SmarttPurchase

/**
 * What the upgrade screen is showing. [active] is the program the user has "bought" during this
 * visit — see [SmarttUpgradeViewModel] for why that is as far as it goes.
 */
data class SmarttUpgradeState(
  val selectedPlan: SmarttPlanId = SmarttPlanId.PRO,
  val period: SmarttBillingPeriod = SmarttBillingPeriod.YEARLY,
  val active: SmarttPurchase? = null,
  val restoreAttempted: Boolean = false
) {
  /** Whether the plan and billing period on screen are exactly what is already subscribed. */
  val isSelectionActive: Boolean
    get() = active != null && active.plan == selectedPlan && active.period == period
}

/**
 * Drives the upgrade screen.
 *
 * Purchasing is a mockup: no billing client, no network, nothing written to disk. State is held
 * here rather than in the composable so it survives configuration changes, and dies with the
 * activity so a relaunch starts from the unsubscribed state again.
 */
class SmarttUpgradeViewModel : ViewModel() {

  private val internalState = mutableStateOf(SmarttUpgradeState())

  val state: State<SmarttUpgradeState> = internalState

  fun selectPlan(plan: SmarttPlanId) {
    internalState.value = internalState.value.copy(selectedPlan = plan)
  }

  fun selectPeriod(period: SmarttBillingPeriod) {
    internalState.value = internalState.value.copy(period = period)
  }

  fun purchase() {
    val current = internalState.value
    internalState.value = current.copy(
      active = SmarttPurchase(current.selectedPlan, current.period),
      restoreAttempted = false
    )
  }

  fun restore() {
    internalState.value = internalState.value.copy(restoreAttempted = true)
  }
}
