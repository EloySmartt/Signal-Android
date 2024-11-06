/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.backup.v2.stream

import com.smarttmessenger.app.backup.v2.proto.BackupInfo
import com.smarttmessenger.app.backup.v2.proto.Frame

interface BackupImportReader : Iterator<Frame>, AutoCloseable {
  fun getHeader(): BackupInfo?
  fun getBytesRead(): Long
  fun getStreamLength(): Long
}
