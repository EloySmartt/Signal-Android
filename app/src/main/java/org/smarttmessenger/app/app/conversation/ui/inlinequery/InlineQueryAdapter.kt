package com.smarttmessenger.app.conversation.ui.inlinequery

import com.smarttmessenger.app.R
import com.smarttmessenger.app.util.adapter.mapping.AnyMappingModel
import com.smarttmessenger.app.util.adapter.mapping.MappingAdapter

class InlineQueryAdapter(listener: (AnyMappingModel) -> Unit) : MappingAdapter() {
  init {
    registerFactory(InlineQueryEmojiResult.Model::class.java, { InlineQueryEmojiResult.ViewHolder(it, listener) }, R.layout.inline_query_emoji_result)
  }
}
