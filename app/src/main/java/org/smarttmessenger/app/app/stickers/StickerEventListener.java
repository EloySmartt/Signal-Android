package com.smarttmessenger.app.stickers;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.database.model.StickerRecord;

public interface StickerEventListener {
  void onStickerSelected(@NonNull StickerRecord sticker);

  void onStickerManagementClicked();
}
