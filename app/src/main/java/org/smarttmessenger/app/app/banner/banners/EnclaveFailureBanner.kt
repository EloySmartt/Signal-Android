/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.banner.banners

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.smarttmessenger.app.R
import com.smarttmessenger.app.banner.Banner
import com.smarttmessenger.app.banner.ui.compose.Action
import com.smarttmessenger.app.banner.ui.compose.DefaultBanner
import com.smarttmessenger.app.banner.ui.compose.Importance
import com.smarttmessenger.app.util.PlayStoreUtil

class EnclaveFailureBanner(enclaveFailed: Boolean, private val context: Context) : Banner() {
  override val enabled: Boolean = enclaveFailed

  @Composable
  override fun DisplayBanner(contentPadding: PaddingValues) {
    DefaultBanner(
      title = null,
      body = stringResource(id = R.string.EnclaveFailureReminder_update_signal),
      importance = Importance.ERROR,
      actions = listOf(
        Action(R.string.ExpiredBuildReminder_update_now) {
          PlayStoreUtil.openPlayStoreOrOurApkDownloadPage(context)
        }
      ),
      paddingValues = contentPadding
    )
  }

  companion object {
    @JvmStatic
    fun Flow<Boolean>.mapBooleanFlowToBannerFlow(context: Context): Flow<EnclaveFailureBanner> {
      return map { EnclaveFailureBanner(it, context) }
    }
  }
}
