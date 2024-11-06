/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.components.settings.app.subscription.donate.stripe

import io.reactivex.rxjava3.core.Single
import org.signal.donations.StripeApi
import org.signal.donations.StripeIntentAccessor
import com.smarttmessenger.app.database.InAppPaymentTable

fun interface StripeNextActionHandler {
  fun handle(
    action: StripeApi.Secure3DSAction,
    inAppPayment: InAppPaymentTable.InAppPayment
  ): Single<StripeIntentAccessor>
}
