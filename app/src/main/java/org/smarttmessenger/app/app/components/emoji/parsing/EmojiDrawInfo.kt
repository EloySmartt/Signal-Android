package com.smarttmessenger.app.components.emoji.parsing

import com.smarttmessenger.app.emoji.EmojiPage

data class EmojiDrawInfo(val page: EmojiPage, val index: Int, val emoji: String, val rawEmoji: String?, val jumboSheet: String?)
