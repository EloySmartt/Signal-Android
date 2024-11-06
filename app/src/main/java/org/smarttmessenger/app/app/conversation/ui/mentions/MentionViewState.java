package com.smarttmessenger.app.conversation.ui.mentions;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.recipients.Recipient;
import com.smarttmessenger.app.util.viewholders.RecipientMappingModel;

public final class MentionViewState extends RecipientMappingModel<MentionViewState> {

  private final Recipient recipient;

  public MentionViewState(@NonNull Recipient recipient) {
    this.recipient = recipient;
  }

  @Override
  public @NonNull Recipient getRecipient() {
    return recipient;
  }
}
