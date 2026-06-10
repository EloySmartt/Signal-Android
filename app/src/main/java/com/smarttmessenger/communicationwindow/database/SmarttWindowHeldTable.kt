package com.smarttmessenger.communicationwindow.database

import android.content.ContentValues
import android.content.Context

/** Tracks which local message IDs are currently held by a communication window. */
class SmarttWindowHeldTable(private val context: Context) {

  private val db get() = SmarttDatabase.getInstance(context).writableDatabase

  fun markAsHeld(messageId: Long, windowOpensAt: Long) {
    val values = ContentValues().apply {
      put(COL_MESSAGE_ID, messageId)
      put(COL_WINDOW_OPENS_AT, windowOpensAt)
    }
    db.insertWithOnConflict(TABLE_NAME, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
  }

  fun clearHeld(messageId: Long) {
    db.delete(TABLE_NAME, "$COL_MESSAGE_ID = ?", arrayOf(messageId.toString()))
  }

  fun getAllHeldMessageIds(): Map<Long, Long> {
    val result = mutableMapOf<Long, Long>()
    db.query(TABLE_NAME, null, null, null, null, null, null).use { cursor ->
      while (cursor.moveToNext()) {
        val msgId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_MESSAGE_ID))
        val opensAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_WINDOW_OPENS_AT))
        result[msgId] = opensAt
      }
    }
    return result
  }

  fun getWindowOpensAt(messageId: Long): Long? {
    db.query(TABLE_NAME, arrayOf(COL_WINDOW_OPENS_AT),
      "$COL_MESSAGE_ID = ?", arrayOf(messageId.toString()),
      null, null, null).use { cursor ->
      if (cursor.moveToFirst()) return cursor.getLong(0)
    }
    return null
  }

  companion object {
    const val TABLE_NAME = "smartt_window_held"
    private const val COL_ID = "_id"
    const val COL_MESSAGE_ID = "message_id"
    const val COL_WINDOW_OPENS_AT = "window_opens_at"

    const val CREATE_TABLE = """
      CREATE TABLE $TABLE_NAME (
        $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COL_MESSAGE_ID INTEGER NOT NULL UNIQUE,
        $COL_WINDOW_OPENS_AT INTEGER NOT NULL DEFAULT 0
      )
    """
  }
}
