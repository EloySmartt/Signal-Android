package com.smarttmessenger.app.keyboard.emoji

import com.smarttmessenger.app.components.emoji.EmojiEventListener
import com.smarttmessenger.app.keyboard.emoji.search.EmojiSearchFragment

interface EmojiKeyboardCallback :
  EmojiEventListener,
  EmojiKeyboardPageFragment.Callback,
  EmojiSearchFragment.Callback
