/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.parental.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import com.smarttmessenger.parental.viewmodel.SmarttParentalViewModel
import org.signal.core.ui.compose.theme.SignalTheme
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme

/**
 * Hosts the Parental Control screen. Every switch lives in the view model so it survives rotation,
 * and resets once this activity finishes.
 */
class SmarttParentalActivity : PassphraseRequiredActivity() {

  private val theme = DynamicNoActionBarTheme()

  private val viewModel: SmarttParentalViewModel by viewModels()

  override fun onPreCreate() {
    theme.onCreate(this)
  }

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    setContent {
      val state by viewModel.state

      SignalTheme {
        SmarttParentalScreen(
          state = state,
          onNavigationClick = { supportFinishAfterTransition() },
          onToggleChanged = viewModel::setToggle,
          onCategoryChanged = viewModel::setCategory,
          onSensitivitySelected = viewModel::setSensitivity,
          onLockClick = viewModel::lock,
          onUnlockClick = viewModel::unlock
        )
      }
    }
  }

  override fun onResume() {
    super.onResume()
    theme.onResume(this)
  }

  companion object {
    fun createIntent(context: Context): Intent = Intent(context, SmarttParentalActivity::class.java)
  }
}
