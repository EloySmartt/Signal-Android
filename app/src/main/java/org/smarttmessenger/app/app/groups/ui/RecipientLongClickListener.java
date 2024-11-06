package com.smarttmessenger.app.groups.ui;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.recipients.Recipient;

public interface RecipientLongClickListener {
  boolean onLongClick(@NonNull Recipient recipient);
}
