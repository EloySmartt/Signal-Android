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
import com.smarttmessenger.app.contacts.sync.CdsPermanentErrorBottomSheet
import com.smarttmessenger.app.keyvalue.SignalStore
import kotlin.time.Duration.Companion.days

class CdsPermanentErrorBanner(private val fragmentManager: FragmentManager) : Banner() {
  private val timeUntilUnblock = SignalStore.misc.cdsBlockedUtil - System.currentTimeMillis()

  override val enabled: Boolean = SignalStore.misc.isCdsBlocked && timeUntilUnblock >= PERMANENT_TIME_CUTOFF

  @Composable
  override fun DisplayBanner(contentPadding: PaddingValues) {
    DefaultBanner(
      title = null,
      body = stringResource(id = R.string.reminder_cds_permanent_error_body),
      importance = Importance.ERROR,
      actions = listOf(
        Action(R.string.reminder_cds_permanent_error_learn_more) {
          CdsPermanentErrorBottomSheet.show(fragmentManager)
        }
      ),
      paddingValues = contentPadding
    )
  }

  companion object {

    /**
     * Even if we're not truly "permanently blocked", if the time until we're unblocked is long enough, we'd rather show the permanent error message than
     * telling the user to wait for 3 months or something.
     */
    val PERMANENT_TIME_CUTOFF = 30.days.inWholeMilliseconds

    @JvmStatic
    fun createFlow(childFragmentManager: FragmentManager): Flow<CdsPermanentErrorBanner> = createAndEmit {
      CdsPermanentErrorBanner(childFragmentManager)
    }
  }
}
