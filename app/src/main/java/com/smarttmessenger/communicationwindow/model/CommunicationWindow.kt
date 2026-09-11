package com.smarttmessenger.communicationwindow.model

import java.time.ZoneId
import java.time.ZonedDateTime

data class CommunicationWindow(
  val windowId: String = "",
  val name: String = "",
  val emoji: String = "",
  val enabled: Boolean = true,
  val schedules: List<CommunicationWindowSchedule> = emptyList(),
  val exceptionContacts: Set<String> = emptySet(),
  val allowCallsFromExceptions: Boolean = true,
  val allowCallsFromAll: Boolean = true,
  val expectations: WindowExpectations = WindowExpectations(),
  /**
   * True when this window has local changes that have not reached our server yet. Read-only as far
   * as callers are concerned: it is populated when reading a row, and what gets *written* is the
   * `needsSync` parameter of `SmarttCommunicationWindowsTable.upsert`, never this field.
   */
  val needsSync: Boolean = false
) {
  fun isCurrentlyActive(timezone: ZoneId = ZoneId.systemDefault()): Boolean {
    if (!enabled) return false
    val now = ZonedDateTime.now(timezone)
    return schedules.any { it.isCurrentlyActive(now) }
  }

  fun activeSchedule(timezone: ZoneId = ZoneId.systemDefault()): CommunicationWindowSchedule? {
    if (!enabled) return null
    val now = ZonedDateTime.now(timezone)
    return schedules.firstOrNull { it.isCurrentlyActive(now) }
  }
}
