package com.smarttmessenger.app.logsubmit;

import android.content.Context;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.AppCapabilities;
import com.smarttmessenger.app.database.SignalDatabase;
import com.smarttmessenger.app.database.model.RecipientRecord;
import com.smarttmessenger.app.keyvalue.SignalStore;
import com.smarttmessenger.app.recipients.Recipient;
import org.whispersystems.signalservice.api.account.AccountAttributes;

public final class LogSectionCapabilities implements LogSection {

  @Override
  public @NonNull String getTitle() {
    return "CAPABILITIES";
  }

  @Override
  public @NonNull CharSequence getContent(@NonNull Context context) {
    if (!SignalStore.account().isRegistered()) {
      return "Unregistered";
    }

    if (SignalStore.account().getE164() == null || SignalStore.account().getAci() == null) {
      return "Self not yet available!";
    }

    Recipient self = Recipient.self();

    AccountAttributes.Capabilities localCapabilities  = AppCapabilities.getCapabilities(false);
    RecipientRecord.Capabilities   globalCapabilities = SignalDatabase.recipients().getCapabilities(self.getId());

    StringBuilder builder = new StringBuilder().append("-- Local").append("\n")
                                               .append("DeleteSync: ").append(localCapabilities.getDeleteSync()).append("\n")
                                               .append("VersionedExpirationTimer: ").append(localCapabilities.getVersionedExpirationTimer()).append("\n")
                                               .append("\n")
                                               .append("-- Global").append("\n");

    if (globalCapabilities != null) {
      builder.append("DeleteSync: ").append(globalCapabilities.getDeleteSync()).append("\n");
      builder.append("\n");
    } else {
      builder.append("Self not found!");
    }

    return builder;
  }
}
