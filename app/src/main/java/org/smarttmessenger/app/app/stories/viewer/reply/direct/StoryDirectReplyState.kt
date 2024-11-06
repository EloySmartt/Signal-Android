package com.smarttmessenger.app.stories.viewer.reply.direct

import com.smarttmessenger.app.database.model.MessageRecord
import com.smarttmessenger.app.recipients.Recipient

data class StoryDirectReplyState(
  val groupDirectReplyRecipient: Recipient? = null,
  val storyRecord: MessageRecord? = null
)
