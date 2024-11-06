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
import com.smarttmessenger.app.R
import com.smarttmessenger.app.banner.Banner
import com.smarttmessenger.app.banner.ui.compose.Action
import com.smarttmessenger.app.banner.ui.compose.DefaultBanner
import com.smarttmessenger.app.banner.ui.compose.Importance
import com.smarttmessenger.app.keyvalue.AccountValues
import com.smarttmessenger.app.keyvalue.AccountValues.UsernameSyncState
import com.smarttmessenger.app.keyvalue.SignalStore

class UsernameOutOfSyncBanner(private val context: Context, private val usernameSyncState: UsernameSyncState, private val onActionClick: (Boolean) -> Unit) : Banner() {

  override val enabled = when (usernameSyncState) {
    AccountValues.UsernameSyncState.USERNAME_AND_LINK_CORRUPTED -> true
    AccountValues.UsernameSyncState.LINK_CORRUPTED -> true
    AccountValues.UsernameSyncState.IN_SYNC -> false
  }

  @Composable
  override fun DisplayBanner(contentPadding: PaddingValues) {
    DefaultBanner(
      title = null,
      body = if (usernameSyncState == UsernameSyncState.USERNAME_AND_LINK_CORRUPTED) {
        stringResource(id = R.string.UsernameOutOfSyncReminder__username_and_link_corrupt)
      } else {
        stringResource(id = R.string.UsernameOutOfSyncReminder__link_corrupt)
      },
      importance = Importance.ERROR,
      actions = listOf(
        Action(R.string.UsernameOutOfSyncReminder__fix_now) {
          onActionClick(usernameSyncState == UsernameSyncState.USERNAME_AND_LINK_CORRUPTED)
        }
      ),
      paddingValues = contentPadding
    )
  }

  companion object {

    /**
     * @param onActionClick input is true if both the username and the link are corrupted, false if only the link is corrupted
     */
    @JvmStatic
    fun createFlow(context: Context, onActionClick: (Boolean) -> Unit): Flow<UsernameOutOfSyncBanner> = createAndEmit {
      UsernameOutOfSyncBanner(context, SignalStore.account.usernameSyncState, onActionClick)
    }
  }
}
