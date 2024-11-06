/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.conversation.v2

import org.signal.paging.ObservablePagedData
import com.smarttmessenger.app.conversation.ConversationData
import com.smarttmessenger.app.conversation.v2.data.ConversationElementKey
import com.smarttmessenger.app.util.adapter.mapping.MappingModel

/**
 * Represents the content that will be displayed in the conversation
 * thread (recycler).
 */
class ConversationThreadState(
  val items: ObservablePagedData<ConversationElementKey, MappingModel<*>>,
  val meta: ConversationData
)
