/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.upgrade.model

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Price arithmetic and formatting for the upgrade screen.
 *
 * Kept free of Compose so it can be exercised directly from tests.
 */
object SmarttPricing {

  /**
   * Formats an amount the way the rest of the pricing UI expects: euro sign, two decimals,
   * comma separator — "4,99" rather than "4.99".
   *
   * The locale is pinned so the output does not drift with the device: formatting with the default
   * locale would already emit a comma in much of Europe, making the separator swap a silent no-op
   * there and a real substitution elsewhere.
   */
  fun format(amount: Double): String = "€" + String.format(Locale.US, "%.2f", amount).replace('.', ',')

  fun price(plan: SmarttPlan, period: SmarttBillingPeriod): Double = when (period) {
    SmarttBillingPeriod.MONTHLY -> plan.monthly
    SmarttBillingPeriod.YEARLY -> plan.yearly
  }

  /** What a yearly subscription works out to per month. */
  fun perMonth(plan: SmarttPlan): Double = plan.yearly / MONTHS_PER_YEAR

  /** How much cheaper a year up front is than twelve monthly payments, as a percentage. */
  fun yearlyDiscountPercent(plan: SmarttPlan): Int =
    ((1 - plan.yearly / (plan.monthly * MONTHS_PER_YEAR)) * 100).roundToInt()

  /** The extra cost of moving from [from] to [to] on the same billing period. */
  fun upgradeDelta(from: SmarttPlan, to: SmarttPlan, period: SmarttBillingPeriod): Double =
    price(to, period) - price(from, period)

  private const val MONTHS_PER_YEAR = 12
}
