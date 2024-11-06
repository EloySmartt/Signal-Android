/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.components.settings.app.changenumber

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.smarttmessenger.app.registration.data.network.Challenge
import com.smarttmessenger.app.registration.ui.captcha.CaptchaFragment

/**
 * Screen visible to the user when they are to solve a captcha. @see [CaptchaFragment]
 */
class ChangeNumberCaptchaFragment : CaptchaFragment() {
  private val viewModel by activityViewModels<ChangeNumberViewModel>()
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.addPresentedChallenge(Challenge.CAPTCHA)
  }

  override fun handleCaptchaToken(token: String) {
    viewModel.setCaptchaResponse(token)
  }

  override fun handleUserExit() {
    viewModel.removePresentedChallenge(Challenge.CAPTCHA)
  }
}
