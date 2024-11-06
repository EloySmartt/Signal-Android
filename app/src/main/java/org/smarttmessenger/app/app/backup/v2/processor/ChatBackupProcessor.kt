/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.backup.v2.processor

import org.signal.core.util.logging.Log
import com.smarttmessenger.app.backup.v2.ExportState
import com.smarttmessenger.app.backup.v2.ImportState
import com.smarttmessenger.app.backup.v2.database.getThreadsForBackup
import com.smarttmessenger.app.backup.v2.database.restoreFromBackup
import com.smarttmessenger.app.backup.v2.proto.Chat
import com.smarttmessenger.app.backup.v2.proto.Frame
import com.smarttmessenger.app.backup.v2.stream.BackupFrameEmitter
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.recipients.RecipientId

object ChatBackupProcessor {
  val TAG = Log.tag(ChatBackupProcessor::class.java)

  fun export(db: SignalDatabase, exportState: ExportState, emitter: BackupFrameEmitter) {
    db.threadTable.getThreadsForBackup().use { reader ->
      for (chat in reader) {
        if (exportState.recipientIds.contains(chat.recipientId)) {
          exportState.threadIds.add(chat.id)
          emitter.emit(Frame(chat = chat))
        } else {
          Log.w(TAG, "dropping thread for deleted recipient ${chat.recipientId}")
        }
      }
    }
  }

  fun import(chat: Chat, importState: ImportState) {
    val recipientId: RecipientId? = importState.remoteToLocalRecipientId[chat.recipientId]
    if (recipientId == null) {
      Log.w(TAG, "Missing recipient for chat ${chat.id}")
      return
    }

    SignalDatabase.threads.restoreFromBackup(chat, recipientId, importState)?.let { threadId ->
      importState.chatIdToLocalRecipientId[chat.id] = recipientId
      importState.chatIdToLocalThreadId[chat.id] = threadId
      importState.chatIdToBackupRecipientId[chat.id] = chat.recipientId
    }
  }
}
