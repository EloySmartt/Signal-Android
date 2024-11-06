/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.backup.v2.stream

import com.smarttmessenger.app.backup.v2.proto.Frame

/**
 * An interface that lets sub-processors emit [Frame]s as they export data.
 */
fun interface BackupFrameEmitter {
  fun emit(frame: Frame)
}
