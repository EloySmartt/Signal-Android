/**
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.service.webrtc.links

/**
 * Result type for call link creation.
 */
sealed interface CreateCallLinkResult {
  data class Success(
    val credentials: CallLinkCredentials,
    val state: SignalCallLinkState
  ) : CreateCallLinkResult

  data class Failure(
    val status: Short
  ) : CreateCallLinkResult
}
