package com.smarttmessenger.app.payments;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.smarttmessenger.app.database.PaymentTable;
import com.smarttmessenger.app.database.SignalDatabase;
import com.smarttmessenger.app.dependencies.AppDependencies;
import com.smarttmessenger.app.keyvalue.SignalStore;
import com.smarttmessenger.app.payments.reconciliation.LedgerReconcile;
import com.smarttmessenger.app.recipients.Recipient;
import com.smarttmessenger.app.util.RemoteConfig;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class DataExportUtil {

  private DataExportUtil() {}

  public static @NonNull String createTsv() {
    if (!RemoteConfig.internalUser()) {
      throw new AssertionError("This is intended for internal use only");
    }

    if (Build.VERSION.SDK_INT < 26) {
      throw new AssertionError();
    }

    List<PaymentTable.PaymentTransaction> paymentTransactions = SignalDatabase.payments().getAll();
    MobileCoinLedgerWrapper               ledger              = SignalStore.payments().liveMobileCoinLedger().getValue();
    List<Payment>                            reconciled          = LedgerReconcile.reconcile(paymentTransactions, Objects.requireNonNull(ledger));

    return createTsv(reconciled);
  }

  @RequiresApi(api = 26)
  private static @NonNull String createTsv(@NonNull List<Payment> payments) {
    Context       context = AppDependencies.getApplication();
    StringBuilder sb      = new StringBuilder();

    sb.append(String.format(Locale.US, "%s\t%s\t%s\t%s\t%s%n", "Date Time", "From", "To", "Amount", "Fee"));

    for (Payment payment : payments) {
      if (payment.getState() != State.SUCCESSFUL) {
        continue;
      }

      String self       = Recipient.self().getDisplayName(context);
      String otherParty = describePayee(context, payment.getPayee());
      String from;
      String to;
      switch (payment.getDirection()) {
        case SENT:
          from = self;
          to = otherParty;
          break;
        case RECEIVED:
          from = otherParty;
          to = self;
          break;
        default:
          throw new AssertionError();
      }
      sb.append(String.format(Locale.US, "%s\t%s\t%s\t%s\t%s%n",
                              DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(payment.getDisplayTimestamp())),
                              from,
                              to,
                              payment.getAmountWithDirection().requireMobileCoin().toBigDecimal(),
                              payment.getFee().requireMobileCoin().toBigDecimal()));
    }
    return sb.toString();
  }

  private static String describePayee(Context context, Payee payee) {
    if (payee.hasRecipientId()) {
      return Recipient.resolved(payee.requireRecipientId()).getDisplayName(context);
    } else if (payee.hasPublicAddress()) {
      return payee.requirePublicAddress().getPaymentAddressBase58();
    } else {
      return "Unknown";
    }
  }
}
