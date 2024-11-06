package com.smarttmessenger.app.payments.backup;

import androidx.annotation.NonNull;

import com.smarttmessenger.app.keyvalue.SignalStore;
import com.smarttmessenger.app.payments.Mnemonic;

public final class PaymentsRecoveryRepository {
  public @NonNull Mnemonic getMnemonic() {
    return SignalStore.payments().getPaymentsMnemonic();
  }
}
