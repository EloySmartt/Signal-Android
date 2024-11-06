/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.banner.banners

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.flow.Flow
import com.smarttmessenger.app.R
import com.smarttmessenger.app.banner.Banner
import com.smarttmessenger.app.banner.ui.compose.Action
import com.smarttmessenger.app.banner.ui.compose.DefaultBanner
import com.smarttmessenger.app.banner.ui.compose.Importance
import com.smarttmessenger.app.contacts.sync.CdsTemporaryErrorBottomSheet
import com.smarttmessenger.app.keyvalue.SignalStore

class CdsTemporaryErrorBanner(private val fragmentManager: FragmentManager) : Banner() {
  private val timeUntilUnblock = SignalStore.misc.cdsBlockedUtil - System.currentTimeMillis()

  override val enabled: Boolean = SignalStore.misc.isCdsBlocked && timeUntilUnblock < CdsPermanentErrorBanner.PERMANENT_TIME_CUTOFF

  @Composable
  override fun DisplayBanner(contentPadding: PaddingValues) {
    DefaultBanner(
      title = null,
      body = stringResource(id = R.string.reminder_cds_warning_body),
      importance = Importance.ERROR,
      actions = listOf(
        Action(R.string.reminder_cds_warning_learn_more) {
          CdsTemporaryErrorBottomSheet.show(fragmentManager)
        }
      ),
      paddingValues = contentPadding
    )
  }

  companion object {

    @JvmStatic
    fun createFlow(childFragmentManager: FragmentManager): Flow<CdsTemporaryErrorBanner> = createAndEmit {
      CdsTemporaryErrorBanner(childFragmentManager)
    }
  }
}
