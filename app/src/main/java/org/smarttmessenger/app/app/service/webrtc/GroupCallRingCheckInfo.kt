package com.smarttmessenger.app.service.webrtc

import org.signal.ringrtc.CallManager
import com.smarttmessenger.app.groups.GroupId
import com.smarttmessenger.app.recipients.RecipientId
import org.whispersystems.signalservice.api.push.ServiceId.ACI

data class GroupCallRingCheckInfo(
  val recipientId: RecipientId,
  val groupId: GroupId.V2,
  val ringId: Long,
  val ringerAci: ACI,
  val ringUpdate: CallManager.RingUpdate
)
