/**
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.calls.links.details

import androidx.compose.runtime.Immutable
import com.smarttmessenger.app.database.CallLinkTable
import com.smarttmessenger.app.service.webrtc.CallLinkPeekInfo

@Immutable
data class CallLinkDetailsState(
  val displayRevocationDialog: Boolean = false,
  val callLink: CallLinkTable.CallLink? = null,
  val peekInfo: CallLinkPeekInfo? = null
)
