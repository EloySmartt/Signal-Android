/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.upgrade.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList

/** The programs a user can subscribe to. */
enum class SmarttPlanId { PRO, PARENTAL }

/** How often a program is billed. */
enum class SmarttBillingPeriod { MONTHLY, YEARLY }

/**
 * A purchasable program. Prices are plain euros; see [SmarttPricing] for formatting.
 */
@Immutable
data class SmarttPlan(
  val id: SmarttPlanId,
  @StringRes val nameRes: Int,
  @StringRes val shortNameRes: Int,
  @StringRes val taglineRes: Int,
  val monthly: Double,
  val yearly: Double
)

/**
 * A subscription the user "owns". UI-only: nothing is ever charged and nothing is persisted,
 * so this lives and dies with the upgrade screen.
 */
@Immutable
data class SmarttPurchase(
  val plan: SmarttPlanId,
  val period: SmarttBillingPeriod
)

/**
 * One selling point, rendered as a tinted icon tile plus copy. [onFreeRes] is the equivalent
 * limit on the free tier, omitted when the feature has no free counterpart at all.
 */
@Immutable
data class SmarttFeature(
  @DrawableRes val iconRes: Int,
  val tint: Color,
  @StringRes val titleRes: Int,
  @StringRes val detailRes: Int,
  @StringRes val onFreeRes: Int? = null
)

/**
 * A titled block of [features]. The block is shown locked and dimmed whenever the selected plan
 * is not [requiredPlan].
 */
@Immutable
data class SmarttFeatureGroup(
  @StringRes val titleRes: Int,
  @StringRes val captionRes: Int,
  val requiredPlan: SmarttPlanId,
  val features: ImmutableList<SmarttFeature>,
  val captionUsesAppName: Boolean = false
)
