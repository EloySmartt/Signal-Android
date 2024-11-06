package com.smarttmessenger.app.mms;


import android.content.Context;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.attachments.Attachment;
import com.smarttmessenger.app.util.MediaUtil;

/**
 * Slide used for attachments with contentType {@link MediaUtil#VIEW_ONCE}.
 * Attachments will only get this type *after* they've been viewed, or if they were synced from a
 * linked device. Incoming unviewed messages will have the appropriate image/video contentType.
 */
public class ViewOnceSlide extends Slide {

  public ViewOnceSlide(@NonNull Attachment attachment) {
    super(attachment);
  }

  @Override
  public boolean hasViewOnce() {
    return true;
  }
}
