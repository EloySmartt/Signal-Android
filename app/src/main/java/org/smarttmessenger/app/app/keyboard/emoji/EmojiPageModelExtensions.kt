package com.smarttmessenger.app.keyboard.emoji

import com.smarttmessenger.app.components.emoji.EmojiPageModel
import com.smarttmessenger.app.components.emoji.EmojiPageViewGridAdapter
import com.smarttmessenger.app.components.emoji.RecentEmojiPageModel
import com.smarttmessenger.app.components.emoji.parsing.EmojiTree
import com.smarttmessenger.app.emoji.EmojiCategory
import com.smarttmessenger.app.emoji.EmojiSource
import com.smarttmessenger.app.util.adapter.mapping.MappingModel

fun EmojiPageModel.toMappingModels(): List<MappingModel<*>> {
  val emojiTree: EmojiTree = EmojiSource.latest.emojiTree

  return displayEmoji.map {
    val isTextEmoji = EmojiCategory.EMOTICONS.key == key || (RecentEmojiPageModel.KEY == key && emojiTree.getEmoji(it.value, 0, it.value.length) == null)

    if (isTextEmoji) {
      EmojiPageViewGridAdapter.EmojiTextModel(key, it)
    } else {
      EmojiPageViewGridAdapter.EmojiModel(key, it)
    }
  }
}
