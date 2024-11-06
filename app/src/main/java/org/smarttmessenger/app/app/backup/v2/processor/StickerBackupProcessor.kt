/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.backup.v2.processor

import okio.ByteString.Companion.toByteString
import org.signal.core.util.Hex
import com.smarttmessenger.app.backup.v2.proto.Frame
import com.smarttmessenger.app.backup.v2.proto.StickerPack
import com.smarttmessenger.app.backup.v2.stream.BackupFrameEmitter
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.database.StickerTable.StickerPackRecordReader
import com.smarttmessenger.app.database.model.StickerPackRecord
import com.smarttmessenger.app.dependencies.AppDependencies
import com.smarttmessenger.app.jobs.StickerPackDownloadJob

object StickerBackupProcessor {
  fun export(db: SignalDatabase, emitter: BackupFrameEmitter) {
    StickerPackRecordReader(db.stickerTable.allStickerPacks).use { reader ->
      var record: StickerPackRecord? = reader.next
      while (record != null) {
        if (record.isInstalled) {
          val frame = record.toBackupFrame()
          emitter.emit(frame)
        }
        record = reader.next
      }
    }
  }

  fun import(stickerPack: StickerPack) {
    AppDependencies.jobManager.add(
      StickerPackDownloadJob.forInstall(Hex.toStringCondensed(stickerPack.packId.toByteArray()), Hex.toStringCondensed(stickerPack.packKey.toByteArray()), false)
    )
  }
}

private fun StickerPackRecord.toBackupFrame(): Frame {
  val packIdBytes = Hex.fromStringCondensed(packId)
  val packKey = Hex.fromStringCondensed(packKey)
  val pack = StickerPack(
    packId = packIdBytes.toByteString(),
    packKey = packKey.toByteString()
  )
  return Frame(stickerPack = pack)
}
