package com.smarttmessenger.app.conversationlist.chatfilter

import com.smarttmessenger.app.conversationlist.model.ConversationFilter

data class ConversationFilterRequest(
  val filter: ConversationFilter,
  val source: ConversationFilterSource
)
