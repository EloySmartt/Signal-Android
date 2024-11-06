/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.components.webrtc.requests

import androidx.compose.runtime.Stable
import com.smarttmessenger.app.recipients.Recipient

data class CallLinkIncomingRequestState(
  val recipient: Recipient = Recipient.UNKNOWN,
  val name: String = "",
  val isSystemContact: Boolean = false,
  val subtitle: String = "",
  @Stable val groupsInCommon: String = ""
)
