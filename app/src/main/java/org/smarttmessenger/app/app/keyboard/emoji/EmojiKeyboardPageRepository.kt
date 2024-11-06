package com.smarttmessenger.app.keyboard.emoji

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import com.smarttmessenger.app.components.emoji.EmojiPageModel
import com.smarttmessenger.app.components.emoji.RecentEmojiPageModel
import com.smarttmessenger.app.emoji.EmojiSource.Companion.latest
import com.smarttmessenger.app.util.TextSecurePreferences
import java.util.function.Consumer

class EmojiKeyboardPageRepository(private val context: Context) {
  fun getEmoji(consumer: Consumer<List<EmojiPageModel>>) {
    SignalExecutors.BOUNDED.execute {
      val list = mutableListOf<EmojiPageModel>()
      list += RecentEmojiPageModel(context, TextSecurePreferences.RECENT_STORAGE_KEY)
      list += latest.displayPages
      consumer.accept(list)
    }
  }
}
