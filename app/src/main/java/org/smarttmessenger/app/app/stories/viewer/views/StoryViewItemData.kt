package com.smarttmessenger.app.stories.viewer.views

import com.smarttmessenger.app.recipients.Recipient

data class StoryViewItemData(
  val recipient: Recipient,
  val timeViewedInMillis: Long
)
