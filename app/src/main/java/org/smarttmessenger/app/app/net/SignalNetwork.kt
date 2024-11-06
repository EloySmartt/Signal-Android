/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.net

import com.smarttmessenger.app.dependencies.AppDependencies
import org.whispersystems.signalservice.api.archive.ArchiveApi
import org.whispersystems.signalservice.api.attachment.AttachmentApi
import org.whispersystems.signalservice.api.keys.KeysApi

/**
 * A convenient way to access network operations, similar to [com.smarttmessenger.app.database.SignalDatabase] and [com.smarttmessenger.app.keyvalue.SignalStore].
 */
object SignalNetwork {
  val archive: ArchiveApi
    get() = AppDependencies.archiveApi

  val attachments: AttachmentApi
    get() = AppDependencies.attachmentApi

  val keys: KeysApi
    get() = AppDependencies.keysApi
}
