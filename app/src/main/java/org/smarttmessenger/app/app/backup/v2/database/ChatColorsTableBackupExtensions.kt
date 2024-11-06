/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.backup.v2.database

import org.signal.core.util.deleteAll
import com.smarttmessenger.app.database.ChatColorsTable

fun ChatColorsTable.clearAllDataForBackupRestore() {
  writableDatabase.deleteAll(ChatColorsTable.TABLE_NAME)
}
