package com.smarttmessenger.communicationwindow.cache

import android.content.Context
import com.smarttmessenger.communicationwindow.database.SmarttWindowHeldTable
import org.signal.core.util.concurrent.SignalExecutors
import org.thoughtcrime.securesms.components.DeliveryStatusView
import org.thoughtcrime.securesms.database.model.MessageRecord
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory cache of held message IDs → windowOpensAt (epoch ms).
 * Loaded from SQLite at startup so the conversation item view holders can check held state
 * without I/O on the UI thread. Cache mutations apply in memory immediately and the matching
 * SQLite write is dispatched to a background executor, since [applyHeldStatus] runs during bind.
 */
object SmarttWindowHeldCache {

  private val cache = ConcurrentHashMap<Long, Long>()

  fun init(context: Context) {
    val table = SmarttWindowHeldTable(context)
    cache.putAll(table.getAllHeldMessageIds())
  }

  fun markAsHeld(context: Context, messageId: Long, windowOpensAt: Long) {
    cache[messageId] = windowOpensAt
    val applicationContext = context.applicationContext
    SignalExecutors.BOUNDED_IO.execute {
      SmarttWindowHeldTable(applicationContext).markAsHeld(messageId, windowOpensAt)
    }
  }

  fun clearHeld(context: Context, messageId: Long) {
    cache.remove(messageId)
    val applicationContext = context.applicationContext
    SignalExecutors.BOUNDED_IO.execute {
      SmarttWindowHeldTable(applicationContext).clearHeld(messageId)
    }
  }

  fun isHeld(messageId: Long): Boolean = cache.containsKey(messageId)

  fun getWindowOpensAt(messageId: Long): Long = cache[messageId] ?: 0L

  /**
   * Applies the window-held delivery status for an outgoing message. Returns true when the cloud
   * icon was shown and normal status rendering should be skipped. Auto-clears the held flag once
   * Signal marks the message delivered or read (the held message reached the recipient).
   * Lives here (not in the view holders) to keep each upstream hook to a single line. Called from
   * both renderers: V2ConversationItemTextOnlyViewHolder (text + V2 media) and ConversationItemFooter
   * (legacy V1 media).
   */
  fun applyHeldStatus(context: Context, messageRecord: MessageRecord, deliveryStatusView: DeliveryStatusView): Boolean {
    if (!messageRecord.isOutgoing || !isHeld(messageRecord.id)) {
      return false
    }
    if (messageRecord.isDelivered || messageRecord.hasReadReceipt()) {
      clearHeld(context, messageRecord.id)
      return false
    }
    deliveryStatusView.setWindowHeld()
    return true
  }
}
