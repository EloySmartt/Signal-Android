/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.backup.v2.stream

import com.smarttmessenger.app.backup.v2.proto.Frame

interface BackupImportStream {
  fun read(): Frame?
}
