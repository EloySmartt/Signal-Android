package com.smarttmessenger.communicationwindow.database

import android.content.ContentValues
import android.content.Context
import com.smarttmessenger.communicationwindow.model.CommunicationWindow
import com.smarttmessenger.communicationwindow.model.CommunicationWindowSchedule
import com.smarttmessenger.communicationwindow.model.WindowExpectations
import org.json.JSONArray
import org.json.JSONObject

/** Local storage for the user's own communication windows. */
class SmarttCommunicationWindowsTable(private val context: Context) {

  private val db get() = SmarttDatabase.getInstance(context).writableDatabase

  /**
   * Local-first write. [needsSync] = true marks the row as pending a background push to our server.
   * This parameter — not [CommunicationWindow.needsSync] — is what gets written; the model field is
   * populated on read only, so a window read back and re-saved cannot silently clear its own flag.
   */
  fun upsert(window: CommunicationWindow, needsSync: Boolean = true) {
    val values = ContentValues().apply {
      put(COL_WINDOW_ID, window.windowId)
      put(COL_NAME, window.name)
      put(COL_EMOJI, window.emoji)
      put(COL_ENABLED, if (window.enabled) 1 else 0)
      put(COL_SCHEDULES_JSON, window.schedules.toJson())
      put(COL_EXCEPTION_CONTACTS_JSON, window.exceptionContacts.toJson())
      put(COL_ALLOW_CALLS_EXCEPTIONS, if (window.allowCallsFromExceptions) 1 else 0)
      put(COL_ALLOW_CALLS_ALL, if (window.allowCallsFromAll) 1 else 0)
      put(COL_CHECK_FREQUENCY, window.expectations.checkFrequency?.name)
      put(COL_USUAL_REPLY_TIME, window.expectations.usualReplyTime?.name)
      put(COL_PERSONAL_NOTE, window.expectations.personalNote)
      put(COL_NEEDS_SYNC, if (needsSync) 1 else 0)
    }
    db.insertWithOnConflict(TABLE_NAME, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    SmarttWindowsObserver.notifyChanged()
  }

  /**
   * Marks a row as successfully pushed to the server. Notifies, because the UI shows an unsynced
   * indicator off [CommunicationWindow.needsSync] and has to drop it once the push lands.
   */
  fun markSynced(windowId: String) {
    val values = ContentValues().apply { put(COL_NEEDS_SYNC, 0) }
    db.update(TABLE_NAME, values, "$COL_WINDOW_ID = ?", arrayOf(windowId))
    SmarttWindowsObserver.notifyChanged()
  }

  /** Windows changed locally but not yet pushed to the server. */
  fun getPendingSync(): List<CommunicationWindow> {
    val result = mutableListOf<CommunicationWindow>()
    db.query(TABLE_NAME, null, "$COL_NEEDS_SYNC = 1", null, null, null, null).use { cursor ->
      while (cursor.moveToNext()) result.add(cursor.toWindow())
    }
    return result
  }

  fun getAll(): List<CommunicationWindow> {
    val result = mutableListOf<CommunicationWindow>()
    db.query(TABLE_NAME, null, null, null, null, null, null).use { cursor ->
      while (cursor.moveToNext()) result.add(cursor.toWindow())
    }
    return result
  }

  fun get(windowId: String): CommunicationWindow? {
    db.query(TABLE_NAME, null, "$COL_WINDOW_ID = ?", arrayOf(windowId), null, null, null).use { cursor ->
      if (cursor.moveToFirst()) return cursor.toWindow()
    }
    return null
  }

  fun delete(windowId: String) {
    db.delete(TABLE_NAME, "$COL_WINDOW_ID = ?", arrayOf(windowId))
    SmarttWindowsObserver.notifyChanged()
  }

  private fun android.database.Cursor.toWindow() = CommunicationWindow(
    windowId = getString(getColumnIndexOrThrow(COL_WINDOW_ID)),
    name = getString(getColumnIndexOrThrow(COL_NAME)) ?: "",
    emoji = getString(getColumnIndexOrThrow(COL_EMOJI)) ?: "",
    enabled = getInt(getColumnIndexOrThrow(COL_ENABLED)) == 1,
    schedules = getString(getColumnIndexOrThrow(COL_SCHEDULES_JSON)).schedulesFromJson(),
    exceptionContacts = getString(getColumnIndexOrThrow(COL_EXCEPTION_CONTACTS_JSON)).contactsFromJson(),
    allowCallsFromExceptions = getInt(getColumnIndexOrThrow(COL_ALLOW_CALLS_EXCEPTIONS)) == 1,
    allowCallsFromAll = getInt(getColumnIndexOrThrow(COL_ALLOW_CALLS_ALL)) == 1,
    expectations = WindowExpectations(
      checkFrequency = getString(getColumnIndexOrThrow(COL_CHECK_FREQUENCY))
        ?.let { runCatching { WindowExpectations.CheckFrequency.valueOf(it) }.getOrNull() },
      usualReplyTime = getString(getColumnIndexOrThrow(COL_USUAL_REPLY_TIME))
        ?.let { runCatching { WindowExpectations.UsualReplyTime.valueOf(it) }.getOrNull() },
      personalNote = getString(getColumnIndexOrThrow(COL_PERSONAL_NOTE))
    ),
    needsSync = getInt(getColumnIndexOrThrow(COL_NEEDS_SYNC)) == 1
  )

  private fun List<CommunicationWindowSchedule>.toJson(): String {
    val arr = JSONArray()
    forEach { s ->
      arr.put(JSONObject().apply {
        put("enabled", s.enabled)
        put("start", s.start)
        put("end", s.end)
        put("daysEnabled", JSONArray(s.daysEnabled.toList()))
      })
    }
    return arr.toString()
  }

  private fun Set<String>.toJson(): String {
    val arr = JSONArray(); forEach { arr.put(it) }; return arr.toString()
  }

  private fun String?.schedulesFromJson(): List<CommunicationWindowSchedule> {
    if (isNullOrBlank()) return emptyList()
    return runCatching {
      val arr = JSONArray(this)
      (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        val days = obj.getJSONArray("daysEnabled")
        CommunicationWindowSchedule(
          enabled = obj.getBoolean("enabled"),
          start = obj.getInt("start"),
          end = obj.getInt("end"),
          daysEnabled = (0 until days.length()).map { days.getInt(it) }.toSet()
        )
      }
    }.getOrDefault(emptyList())
  }

  private fun String?.contactsFromJson(): Set<String> {
    if (isNullOrBlank()) return emptySet()
    return runCatching {
      val arr = JSONArray(this)
      (0 until arr.length()).map { arr.getString(it) }.toSet()
    }.getOrDefault(emptySet())
  }

  companion object {
    const val TABLE_NAME = "smartt_communication_windows"
    private const val COL_ID = "_id"
    const val COL_WINDOW_ID = "window_id"
    const val COL_NAME = "name"
    const val COL_EMOJI = "emoji"
    const val COL_ENABLED = "enabled"
    const val COL_SCHEDULES_JSON = "schedules_json"
    const val COL_EXCEPTION_CONTACTS_JSON = "exception_contacts_json"
    const val COL_ALLOW_CALLS_EXCEPTIONS = "allow_calls_from_exceptions"
    const val COL_ALLOW_CALLS_ALL = "allow_calls_from_all"
    const val COL_CHECK_FREQUENCY = "check_frequency"
    const val COL_USUAL_REPLY_TIME = "usual_reply_time"
    const val COL_PERSONAL_NOTE = "personal_note"
    const val COL_NEEDS_SYNC = "needs_sync"

    const val CREATE_TABLE = """
      CREATE TABLE $TABLE_NAME (
        $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COL_WINDOW_ID TEXT NOT NULL UNIQUE,
        $COL_NAME TEXT NOT NULL DEFAULT '',
        $COL_EMOJI TEXT,
        $COL_ENABLED INTEGER NOT NULL DEFAULT 1,
        $COL_SCHEDULES_JSON TEXT NOT NULL DEFAULT '[]',
        $COL_EXCEPTION_CONTACTS_JSON TEXT NOT NULL DEFAULT '[]',
        $COL_ALLOW_CALLS_EXCEPTIONS INTEGER NOT NULL DEFAULT 1,
        $COL_ALLOW_CALLS_ALL INTEGER NOT NULL DEFAULT 1,
        $COL_CHECK_FREQUENCY TEXT,
        $COL_USUAL_REPLY_TIME TEXT,
        $COL_PERSONAL_NOTE TEXT,
        $COL_NEEDS_SYNC INTEGER NOT NULL DEFAULT 0
      )
    """
  }
}
