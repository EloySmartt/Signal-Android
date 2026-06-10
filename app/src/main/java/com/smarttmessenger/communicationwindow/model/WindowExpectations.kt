package com.smarttmessenger.communicationwindow.model

data class WindowExpectations(
  val checkFrequency: CheckFrequency? = null,
  val usualReplyTime: UsualReplyTime? = null,
  val personalNote: String? = null
) {
  enum class CheckFrequency(val label: String) {
    EVERY_FEW_MINUTES("Every few minutes"),
    ONCE_AN_HOUR("Once an hour"),
    EVERY_FEW_HOURS("Every few hours"),
    ONCE_A_DAY("Once a day")
  }

  enum class UsualReplyTime(val label: String) {
    WITHIN_MINUTES("Within minutes"),
    WITHIN_AN_HOUR("Within an hour"),
    IN_THE_AFTERNOON("In the afternoon"),
    IN_THE_EVENING("In the evening"),
    NEXT_DAY("Next day"),
    NEXT_BUSINESS_DAY("Next business day")
  }
}
