package com.smarttmessenger.mail.model

import com.smarttmessenger.communicationwindow.model.CommunicationWindowSchedule
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * When fetched mail is *revealed* to the user.
 *
 * Reuses [CommunicationWindowSchedule] (the communication-window model) on purpose, so mail delivery
 * follows the exact same window semantics as messages: mail is held while every schedule is closed
 * and released when a schedule opens.
 *
 *  - [scheduled] == true  -> held until a window opens.
 *  - [scheduled] == false -> real-time (shown as soon as it is fetched).
 */
data class MailDeliveryWindow(
  val scheduled: Boolean = true,
  val schedules: List<CommunicationWindowSchedule> = listOf(
    CommunicationWindowSchedule(start = 9 * 60, end = 12 * 60),   // morning
    CommunicationWindowSchedule(start = 15 * 60, end = 18 * 60)   // afternoon
  )
) {
  fun isOpenNow(now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())): Boolean {
    if (!scheduled) return true
    return schedules.any { it.isCurrentlyActive(now) }
  }
}
