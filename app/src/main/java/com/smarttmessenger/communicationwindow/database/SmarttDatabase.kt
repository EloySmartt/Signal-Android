package com.smarttmessenger.communicationwindow.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Separate SQLite database for all Smartt custom data.
 * Completely isolated from Signal's main database.
 */
class SmarttDatabase private constructor(context: Context) :
  SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(SmarttWindowHeldTable.CREATE_TABLE)
    db.execSQL(SmarttCommunicationWindowsTable.CREATE_TABLE)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    if (oldVersion < 2) {
      // Local-first sync flag: 1 = changed locally, not yet pushed to our server.
      db.execSQL("ALTER TABLE ${SmarttCommunicationWindowsTable.TABLE_NAME} ADD COLUMN ${SmarttCommunicationWindowsTable.COL_NEEDS_SYNC} INTEGER NOT NULL DEFAULT 0")
    }
  }

  companion object {
    private const val DATABASE_NAME = "smartt_data.db"
    private const val DATABASE_VERSION = 2

    @Volatile private var instance: SmarttDatabase? = null

    fun getInstance(context: Context): SmarttDatabase =
      instance ?: synchronized(this) {
        instance ?: SmarttDatabase(context.applicationContext).also { instance = it }
      }
  }
}
