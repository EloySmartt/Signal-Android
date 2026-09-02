/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.parental.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableSet

/**
 * Every switch on the Parental Control screen. Held as a set of the ones currently on, so the
 * screen never has to thread a dozen separate booleans around.
 */
enum class SmarttParentalToggle {
  SCAN_OUTGOING,
  KEEP_LOG,
  CHILD_CAN_FLAG,
  WINDOWS,
  BEDTIME_LOCK,
  CALLS_FOLLOW_WINDOWS,
  APPROVED_CONTACTS_ONLY,
  APPROVED_MAIL_ONLY,
  WEEKLY_DIGEST,
  REQUIRE_PIN
}

/** The kinds of content the on-device filter can hold back. */
enum class SmarttFilterCategory {
  SEXUAL,
  VIOLENCE,
  WEAPONS,
  DRUGS,
  SELF_HARM,
  HATE,
  SCAMS,
  PROFANITY
}

/** How eagerly the filter flags borderline media. */
enum class SmarttFilterSensitivity { LOW, BALANCED, STRICT }

/**
 * A switch with a fixed description. Switches whose detail line changes with state — the windows
 * master — are rendered directly by their section instead.
 */
@Immutable
data class SmarttParentalToggleRow(
  val toggle: SmarttParentalToggle,
  @DrawableRes val iconRes: Int,
  val tint: Color,
  @StringRes val titleRes: Int,
  @StringRes val detailRes: Int
)

/** One row of the "What to block" card. */
@Immutable
data class SmarttParentalCategoryRow(
  val category: SmarttFilterCategory,
  @DrawableRes val iconRes: Int,
  val tint: Color,
  @StringRes val titleRes: Int,
  @StringRes val detailRes: Int
)

/**
 * A window during which the child's phone is allowed to show what has arrived.
 *
 * Read-only in this mockup — the screen displays the presets and offers no way to edit them, so
 * there is no identity to track and the times stay plain display strings.
 *
 * [days] holds day indices where 0 is Monday.
 */
@Immutable
data class SmarttChildWindow(
  @StringRes val labelRes: Int,
  val start: String,
  val end: String,
  val days: ImmutableSet<Int>
)
