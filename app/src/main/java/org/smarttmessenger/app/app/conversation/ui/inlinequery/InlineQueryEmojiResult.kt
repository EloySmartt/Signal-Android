package com.smarttmessenger.app.conversation.ui.inlinequery

import android.view.View
import com.smarttmessenger.app.R
import com.smarttmessenger.app.components.emoji.EmojiImageView
import com.smarttmessenger.app.util.adapter.mapping.AnyMappingModel
import com.smarttmessenger.app.util.adapter.mapping.MappingModel
import com.smarttmessenger.app.util.adapter.mapping.MappingViewHolder

/**
 * Used to render inline emoji search results in a [com.smarttmessenger.app.util.adapter.mapping.MappingAdapter]
 */
object InlineQueryEmojiResult {

  class Model(val canonicalEmoji: String, val preferredEmoji: String) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean {
      return canonicalEmoji == newItem.canonicalEmoji
    }

    override fun areContentsTheSame(newItem: Model): Boolean {
      return preferredEmoji == newItem.preferredEmoji
    }
  }

  class ViewHolder(itemView: View, private val listener: (AnyMappingModel) -> Unit) : MappingViewHolder<Model>(itemView) {

    private val emoji: EmojiImageView = findViewById(R.id.inline_query_emoji_image)

    override fun bind(model: Model) {
      itemView.setOnClickListener { listener(model) }
      emoji.setImageEmoji(model.preferredEmoji)
    }
  }
}
