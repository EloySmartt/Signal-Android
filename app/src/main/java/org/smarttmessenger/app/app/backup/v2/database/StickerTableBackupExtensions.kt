/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.backup.v2.database

import org.signal.core.util.deleteAll
import com.smarttmessenger.app.database.StickerTable

fun StickerTable.clearAllDataForBackupRestore() {
  writableDatabase.deleteAll(StickerTable.TABLE_NAME)
}
