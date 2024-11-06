package com.smarttmessenger.app.storage;

import androidx.annotation.NonNull;

/**
 * Generates a key for use with the storage service.
 */
interface StorageKeyGenerator {
  @NonNull
  byte[] generate();
}
