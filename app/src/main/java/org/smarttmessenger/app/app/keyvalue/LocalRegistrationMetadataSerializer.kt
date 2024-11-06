/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.keyvalue

import org.signal.core.util.ByteSerializer
import com.smarttmessenger.app.database.model.databaseprotos.LocalRegistrationMetadata

/**
 * Serialize [LocalRegistrationMetadata]
 */
object LocalRegistrationMetadataSerializer : ByteSerializer<LocalRegistrationMetadata> {
  override fun serialize(data: LocalRegistrationMetadata): ByteArray = data.encode()
  override fun deserialize(data: ByteArray): LocalRegistrationMetadata = LocalRegistrationMetadata.ADAPTER.decode(data)
}
