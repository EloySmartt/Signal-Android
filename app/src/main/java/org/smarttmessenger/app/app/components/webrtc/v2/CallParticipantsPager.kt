/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.components.webrtc.v2

import android.content.res.Configuration
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.smarttmessenger.app.R
import com.smarttmessenger.app.components.webrtc.CallParticipantsLayout
import com.smarttmessenger.app.components.webrtc.CallParticipantsLayoutStrategies
import com.smarttmessenger.app.events.CallParticipant

@Composable
fun CallParticipantsPager(
  callParticipantsPagerState: CallParticipantsPagerState,
  modifier: Modifier = Modifier
) {
  CallParticipantsLayoutComponent(
    callParticipantsPagerState = callParticipantsPagerState,
    modifier = modifier
  )
}

@Composable
private fun CallParticipantsLayoutComponent(
  callParticipantsPagerState: CallParticipantsPagerState,
  modifier: Modifier = Modifier
) {
  if (callParticipantsPagerState.focusedParticipant == null) {
    return
  }

  val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

  AndroidView(
    factory = {
      LayoutInflater.from(it).inflate(R.layout.webrtc_call_participants_layout, FrameLayout(it), false) as CallParticipantsLayout
    },
    modifier = modifier
  ) {
    it.update(
      callParticipantsPagerState.callParticipants,
      callParticipantsPagerState.focusedParticipant,
      callParticipantsPagerState.isRenderInPip,
      isPortrait,
      callParticipantsPagerState.hideAvatar,
      0,
      CallParticipantsLayoutStrategies.getStrategy(isPortrait, true)
    )
  }
}

@Immutable
data class CallParticipantsPagerState(
  val callParticipants: List<CallParticipant> = emptyList(),
  val focusedParticipant: CallParticipant? = null,
  val isRenderInPip: Boolean = false,
  val hideAvatar: Boolean = false
)
