package com.smarttmessenger.app.mms;


import android.content.Context;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.attachments.Attachment;

public class MmsSlide extends ImageSlide {

  public MmsSlide(@NonNull Attachment attachment) {
    super(attachment);
  }

  @NonNull
  @Override
  public String getContentDescription(Context context) {
    return "MMS";
  }

}
