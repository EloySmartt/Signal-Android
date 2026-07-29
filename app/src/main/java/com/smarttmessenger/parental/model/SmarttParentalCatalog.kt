/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.parental.model

import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.thoughtcrime.securesms.R

/**
 * What the Parental Control screen shows. Static by design: nothing here is enforced yet, so the
 * categories, windows and counters are declared rather than read from the device.
 *
 * Tints are translucent so they sit correctly on both the light and dark card surfaces. They mirror
 * the values used by the upgrade screen's feature tiles.
 */
object SmarttParentalCatalog {

  const val BLOCKED_COUNT = 37
  const val WAITING_COUNT = 4
  const val REVIEW_COUNT = 4

  private val TINT_INDIGO = Color(0x294655FF)
  private val TINT_PINK = Color(0x29FF5C8A)
  private val TINT_ORANGE = Color(0x2EFF8A3D)
  private val TINT_PERIWINKLE = Color(0x2E7884FF)
  private val TINT_GREEN = Color(0x2E5ABE8C)
  private val TINT_VIOLET = Color(0x2EA078FF)
  private val TINT_BLUE = Color(0x2E50AAFF)
  private val TINT_GREY = Color(0x2E969696)
  private val TINT_CORAL = Color(0x2EFF6B6B)

  /** Tint of the two section master switches and the parent PIN switch. */
  val masterTint: Color = TINT_INDIGO

  /** Day letters for the window chips, starting on Monday. */
  val dayLetters: ImmutableList<Int> = persistentListOf(
    R.string.CommunicationWindow__monday_first_letter,
    R.string.CommunicationWindow__tuesday_first_letter,
    R.string.CommunicationWindow__wednesday_first_letter,
    R.string.CommunicationWindow__thursday_first_letter,
    R.string.CommunicationWindow__friday_first_letter,
    R.string.CommunicationWindow__saturday_first_letter,
    R.string.CommunicationWindow__sunday_first_letter
  )

  val filterCategories: ImmutableList<SmarttParentalCategoryRow> = persistentListOf(
    SmarttParentalCategoryRow(
      category = SmarttFilterCategory.SEXUAL,
      iconRes = R.drawable.smartt_symbol_eye_20,
      tint = TINT_PINK,
      titleRes = R.string.SmarttParental_category__sexual_title,
      detailRes = R.string.SmarttParental_category__sexual_detail
    ),
    SmarttParentalCategoryRow(
      category = SmarttFilterCategory.VIOLENCE,
      iconRes = R.drawable.symbol_error_triangle_fill_24,
      tint = TINT_ORANGE,
      titleRes = R.string.SmarttParental_category__violence_title,
      detailRes = R.string.SmarttParental_category__violence_detail
    ),
    SmarttParentalCategoryRow(
      category = SmarttFilterCategory.WEAPONS,
      iconRes = R.drawable.smartt_symbol_shield_20,
      tint = TINT_PERIWINKLE,
      titleRes = R.string.SmarttParental_category__weapons_title,
      detailRes = R.string.SmarttParental_category__weapons_detail
    ),
    SmarttParentalCategoryRow(
      category = SmarttFilterCategory.DRUGS,
      iconRes = R.drawable.smartt_symbol_bottle_20,
      tint = TINT_GREEN,
      titleRes = R.string.SmarttParental_category__drugs_title,
      detailRes = R.string.SmarttParental_category__drugs_detail
    ),
    SmarttParentalCategoryRow(
      category = SmarttFilterCategory.SELF_HARM,
      iconRes = R.drawable.symbol_heart_24,
      tint = TINT_CORAL,
      titleRes = R.string.SmarttParental_category__selfharm_title,
      detailRes = R.string.SmarttParental_category__selfharm_detail
    ),
    SmarttParentalCategoryRow(
      category = SmarttFilterCategory.HATE,
      iconRes = R.drawable.symbol_group_24,
      tint = TINT_VIOLET,
      titleRes = R.string.SmarttParental_category__hate_title,
      detailRes = R.string.SmarttParental_category__hate_detail
    ),
    SmarttParentalCategoryRow(
      category = SmarttFilterCategory.SCAMS,
      iconRes = R.drawable.smartt_symbol_globe_20,
      tint = TINT_BLUE,
      titleRes = R.string.SmarttParental_category__scams_title,
      detailRes = R.string.SmarttParental_category__scams_detail
    ),
    SmarttParentalCategoryRow(
      category = SmarttFilterCategory.PROFANITY,
      iconRes = R.drawable.symbol_note_24,
      tint = TINT_GREY,
      titleRes = R.string.SmarttParental_category__profanity_title,
      detailRes = R.string.SmarttParental_category__profanity_detail
    )
  )

  /** Filter options that sit below the category list and share its enabled state. */
  val filterExtraToggles: ImmutableList<SmarttParentalToggleRow> = persistentListOf(
    SmarttParentalToggleRow(
      toggle = SmarttParentalToggle.SCAN_OUTGOING,
      iconRes = R.drawable.symbol_camera_24,
      tint = TINT_ORANGE,
      titleRes = R.string.SmarttParental_filter__scan_outgoing_title,
      detailRes = R.string.SmarttParental_filter__scan_outgoing_detail
    ),
    SmarttParentalToggleRow(
      toggle = SmarttParentalToggle.KEEP_LOG,
      iconRes = R.drawable.symbol_archive_24,
      tint = TINT_BLUE,
      titleRes = R.string.SmarttParental_filter__keep_log_title,
      detailRes = R.string.SmarttParental_filter__keep_log_detail
    ),
    SmarttParentalToggleRow(
      toggle = SmarttParentalToggle.CHILD_CAN_FLAG,
      iconRes = R.drawable.symbol_person_24,
      tint = TINT_GREY,
      titleRes = R.string.SmarttParental_filter__child_flag_title,
      detailRes = R.string.SmarttParental_filter__child_flag_detail
    )
  )

  val windowExtraToggles: ImmutableList<SmarttParentalToggleRow> = persistentListOf(
    SmarttParentalToggleRow(
      toggle = SmarttParentalToggle.BEDTIME_LOCK,
      iconRes = R.drawable.symbol_bell_slash_24,
      tint = TINT_PERIWINKLE,
      titleRes = R.string.SmarttParental_windows__bedtime_title,
      detailRes = R.string.SmarttParental_windows__bedtime_detail
    ),
    SmarttParentalToggleRow(
      toggle = SmarttParentalToggle.CALLS_FOLLOW_WINDOWS,
      iconRes = R.drawable.symbol_phone_24,
      tint = TINT_GREEN,
      titleRes = R.string.SmarttParental_windows__calls_title,
      detailRes = R.string.SmarttParental_windows__calls_detail
    )
  )

  val contactToggles: ImmutableList<SmarttParentalToggleRow> = persistentListOf(
    SmarttParentalToggleRow(
      toggle = SmarttParentalToggle.APPROVED_CONTACTS_ONLY,
      iconRes = R.drawable.symbol_person_24,
      tint = TINT_BLUE,
      titleRes = R.string.SmarttParental_contacts__approved_title,
      detailRes = R.string.SmarttParental_contacts__approved_detail
    ),
    SmarttParentalToggleRow(
      toggle = SmarttParentalToggle.APPROVED_MAIL_ONLY,
      iconRes = R.drawable.smartt_symbol_mail_20,
      tint = TINT_ORANGE,
      titleRes = R.string.SmarttParental_contacts__mail_title,
      detailRes = R.string.SmarttParental_contacts__mail_detail
    ),
    SmarttParentalToggleRow(
      toggle = SmarttParentalToggle.WEEKLY_DIGEST,
      iconRes = R.drawable.symbol_data_24,
      tint = TINT_GREY,
      titleRes = R.string.SmarttParental_contacts__digest_title,
      detailRes = R.string.SmarttParental_contacts__digest_detail
    )
  )

  val requirePinToggle: SmarttParentalToggleRow = SmarttParentalToggleRow(
    toggle = SmarttParentalToggle.REQUIRE_PIN,
    iconRes = R.drawable.symbol_lock_24,
    tint = TINT_INDIGO,
    titleRes = R.string.SmarttParental_pin__require_title,
    detailRes = R.string.SmarttParental_pin__require_detail
  )

  val windows: ImmutableList<SmarttChildWindow> = persistentListOf(
    SmarttChildWindow(
      labelRes = R.string.SmarttParental_windows__after_school,
      start = "16:30",
      end = "18:30",
      days = persistentSetOf(0, 1, 2, 3, 4)
    ),
    SmarttChildWindow(
      labelRes = R.string.SmarttParental_windows__weekend,
      start = "10:00",
      end = "13:00",
      days = persistentSetOf(5, 6)
    )
  )

  val allowedContacts: ImmutableList<Int> = persistentListOf(
    R.string.SmarttParental_windows__allowed_mum,
    R.string.SmarttParental_windows__allowed_dad,
    R.string.SmarttParental_windows__allowed_grandma,
    R.string.SmarttParental_windows__allowed_emergency
  )

  /** Every switch starts on. */
  val DEFAULT_TOGGLES: ImmutableSet<SmarttParentalToggle> = persistentSetOf(
    SmarttParentalToggle.FILTER,
    SmarttParentalToggle.SCAN_OUTGOING,
    SmarttParentalToggle.KEEP_LOG,
    SmarttParentalToggle.CHILD_CAN_FLAG,
    SmarttParentalToggle.WINDOWS,
    SmarttParentalToggle.BEDTIME_LOCK,
    SmarttParentalToggle.CALLS_FOLLOW_WINDOWS,
    SmarttParentalToggle.APPROVED_CONTACTS_ONLY,
    SmarttParentalToggle.APPROVED_MAIL_ONLY,
    SmarttParentalToggle.WEEKLY_DIGEST,
    SmarttParentalToggle.REQUIRE_PIN
  )

  /** Everything except profanity, which only masks words rather than hiding media. */
  val DEFAULT_CATEGORIES: ImmutableSet<SmarttFilterCategory> = persistentSetOf(
    SmarttFilterCategory.SEXUAL,
    SmarttFilterCategory.VIOLENCE,
    SmarttFilterCategory.WEAPONS,
    SmarttFilterCategory.DRUGS,
    SmarttFilterCategory.SELF_HARM,
    SmarttFilterCategory.HATE,
    SmarttFilterCategory.SCAMS
  )
}
