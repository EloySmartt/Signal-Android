/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.migrations

import com.smarttmessenger.app.components.settings.app.subscription.InAppPaymentsRepository.toPaymentMethodType
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.database.model.InAppPaymentSubscriberRecord
import com.smarttmessenger.app.jobmanager.Job
import com.smarttmessenger.app.keyvalue.SignalStore
import java.util.Currency

/**
 * Migrates all subscriber ids from the key value store into the database.
 */
internal class SubscriberIdMigrationJob(
  parameters: Parameters = Parameters.Builder().build()
) : MigrationJob(
  parameters
) {

  companion object {
    const val KEY = "SubscriberIdMigrationJob"
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    Currency.getAvailableCurrencies().forEach { currency ->
      val subscriber = SignalStore.inAppPayments.getSubscriber(currency)

      if (subscriber != null) {
        SignalDatabase.inAppPaymentSubscribers.insertOrReplace(
          InAppPaymentSubscriberRecord(
            subscriber.subscriberId,
            subscriber.currency,
            InAppPaymentSubscriberRecord.Type.DONATION,
            SignalStore.inAppPayments.shouldCancelSubscriptionBeforeNextSubscribeAttempt,
            SignalStore.inAppPayments.getSubscriptionPaymentSourceType().toPaymentMethodType()
          )
        )
      }
    }
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<SubscriberIdMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): SubscriberIdMigrationJob {
      return SubscriberIdMigrationJob(parameters)
    }
  }
}
