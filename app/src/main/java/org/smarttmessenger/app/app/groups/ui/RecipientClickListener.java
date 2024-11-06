package com.smarttmessenger.app.groups.ui;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.recipients.Recipient;

public interface RecipientClickListener {
  void onClick(@NonNull Recipient recipient);
}
