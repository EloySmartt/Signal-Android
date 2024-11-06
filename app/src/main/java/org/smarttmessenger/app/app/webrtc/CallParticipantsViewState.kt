package com.smarttmessenger.app.webrtc

import com.smarttmessenger.app.components.webrtc.CallParticipantsState
import com.smarttmessenger.app.service.webrtc.state.WebRtcEphemeralState

class CallParticipantsViewState(
  callParticipantsState: CallParticipantsState,
  ephemeralState: WebRtcEphemeralState,
  val isPortrait: Boolean,
  val isLandscapeEnabled: Boolean,
  val isStartedFromCallLink: Boolean
) {

  val callParticipantsState = CallParticipantsState.update(callParticipantsState, ephemeralState)
}
