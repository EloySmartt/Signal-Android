package com.smarttmessenger.app.stories.settings.group

import com.smarttmessenger.app.recipients.Recipient

data class GroupStorySettingsState(
  val name: String = "",
  val members: List<Recipient> = emptyList(),
  val removed: Boolean = false
)
