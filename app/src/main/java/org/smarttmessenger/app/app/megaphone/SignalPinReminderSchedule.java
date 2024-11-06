package com.smarttmessenger.app.megaphone;

import com.smarttmessenger.app.keyvalue.SignalStore;

final class SignalPinReminderSchedule implements MegaphoneSchedule {

  @Override
  public boolean shouldDisplay(int seenCount, long lastSeen, long firstVisible, long currentTime) {
    if (SignalStore.svr().hasOptedOut()) {
      return false;
    }

    if (!SignalStore.svr().hasPin()) {
      return false;
    }

    if (!SignalStore.pin().arePinRemindersEnabled()) {
      return false;
    }

    if (!SignalStore.account().isRegistered()) {
      return false;
    }

    long lastSuccessTime = SignalStore.pin().getLastSuccessfulEntryTime();
    long interval        = SignalStore.pin().getCurrentInterval();

    return currentTime - lastSuccessTime >= interval;
  }
}
