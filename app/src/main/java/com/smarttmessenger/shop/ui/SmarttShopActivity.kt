/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.shop.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import org.signal.core.ui.compose.theme.SignalTheme
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.util.CommunicationActions
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme

/**
 * Hosts the shop. Stateless — every product link hands off to the browser.
 */
class SmarttShopActivity : PassphraseRequiredActivity() {

  private val theme = DynamicNoActionBarTheme()

  override fun onPreCreate() {
    theme.onCreate(this)
  }

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    setContent {
      SignalTheme {
        SmarttShopScreen(
          onNavigationClick = { supportFinishAfterTransition() },
          onOpenLink = { url -> CommunicationActions.openBrowserLink(this, url) }
        )
      }
    }
  }

  override fun onResume() {
    super.onResume()
    theme.onResume(this)
  }

  companion object {
    fun createIntent(context: Context): Intent = Intent(context, SmarttShopActivity::class.java)
  }
}
