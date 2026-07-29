/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.upgrade.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import com.smarttmessenger.upgrade.viewmodel.SmarttUpgradeViewModel
import org.signal.core.ui.compose.theme.SignalTheme
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme

/**
 * Hosts the upgrade screen. The selected plan and the mock purchase live in the view model so they
 * survive rotation, and are gone once this activity finishes.
 */
class SmarttUpgradeActivity : PassphraseRequiredActivity() {

  private val theme = DynamicNoActionBarTheme()

  private val viewModel: SmarttUpgradeViewModel by viewModels()

  override fun onPreCreate() {
    theme.onCreate(this)
  }

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    setContent {
      val state by viewModel.state

      SignalTheme {
        SmarttUpgradeScreen(
          state = state,
          onNavigationClick = { supportFinishAfterTransition() },
          onPlanSelected = viewModel::selectPlan,
          onPeriodSelected = viewModel::selectPeriod,
          onPurchaseClick = viewModel::purchase,
          onRestoreClick = viewModel::restore
        )
      }
    }
  }

  override fun onResume() {
    super.onResume()
    theme.onResume(this)
  }

  companion object {
    fun createIntent(context: Context): Intent = Intent(context, SmarttUpgradeActivity::class.java)
  }
}
