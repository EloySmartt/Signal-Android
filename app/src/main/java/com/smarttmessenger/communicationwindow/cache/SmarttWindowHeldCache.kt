package com.smarttmessenger.communicationwindow.cache

import android.content.Context
import com.smarttmessenger.communicationwindow.database.SmarttWindowHeldTable
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory cache of held message IDs → windowOpensAt (epoch ms).
 * Loaded from SQLite at startup so ConversationItemFooter can check
 * held state without I/O on the UI thread.
 */
object SmarttWindowHeldCache {

  private val cache = ConcurrentHashMap<Long, Long>()

  fun init(context: Context) {
    val table = SmarttWindowHeldTable(context)
    cache.putAll(table.getAllHeldMessageIds())
  }

  fun markAsHeld(context: Context, messageId: Long, windowOpensAt: Long) {
    cache[messageId] = windowOpensAt
    SmarttWindowHeldTable(context).markAsHeld(messageId, windowOpensAt)
  }

  fun clearHeld(context: Context, messageId: Long) {
    cache.remove(messageId)
    SmarttWindowHeldTable(context).clearHeld(messageId)
  }

  fun isHeld(messageId: Long): Boolean = cache.containsKey(messageId)

  fun getWindowOpensAt(messageId: Long): Long = cache[messageId] ?: 0L
}
