/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.upgrade.model

import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.thoughtcrime.securesms.R

/**
 * What the upgrade screen sells. Static by design: there is no billing backend behind this yet,
 * so prices and copy are declared here rather than fetched.
 */
object SmarttUpgradeCatalog {

  const val TRIAL_DAYS = 7

  private val TINT_INDIGO = Color(0x294655FF)
  private val TINT_ORANGE = Color(0x2EFF8A3D)
  private val TINT_BLUE = Color(0x2E50AAFF)
  private val TINT_GREEN = Color(0x2E5ABE8C)
  private val TINT_VIOLET = Color(0x2EA078FF)
  private val TINT_PERIWINKLE = Color(0x2E7884FF)
  private val TINT_PINK = Color(0x29FF5C8A)
  private val TINT_GREY = Color(0x2E969696)

  val plans: ImmutableList<SmarttPlan> = persistentListOf(
    SmarttPlan(
      id = SmarttPlanId.PRO,
      nameRes = R.string.SmarttUpgrade_plan__pro_name,
      shortNameRes = R.string.SmarttUpgrade_plan__pro_short_name,
      taglineRes = R.string.SmarttUpgrade_plan__pro_tagline,
      monthly = 4.99,
      yearly = 39.99
    ),
    SmarttPlan(
      id = SmarttPlanId.PARENTAL,
      nameRes = R.string.SmarttUpgrade_plan__parental_name,
      shortNameRes = R.string.SmarttUpgrade_plan__parental_short_name,
      taglineRes = R.string.SmarttUpgrade_plan__parental_tagline,
      monthly = 9.99,
      yearly = 79.99
    )
  )

  val featureGroups: ImmutableList<SmarttFeatureGroup> = persistentListOf(
    SmarttFeatureGroup(
      titleRes = R.string.SmarttUpgrade_group__windows_title,
      captionRes = R.string.SmarttUpgrade_group__windows_caption,
      captionUsesAppName = true,
      requiredPlan = SmarttPlanId.PRO,
      features = persistentListOf(
        SmarttFeature(
          iconRes = R.drawable.symbol_timer_24,
          tint = TINT_INDIGO,
          titleRes = R.string.SmarttUpgrade_feature__unlimited_windows_title,
          detailRes = R.string.SmarttUpgrade_feature__unlimited_windows_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__unlimited_windows_free
        ),
        SmarttFeature(
          iconRes = R.drawable.smartt_symbol_mail_20,
          tint = TINT_ORANGE,
          titleRes = R.string.SmarttUpgrade_feature__mail_accounts_title,
          detailRes = R.string.SmarttUpgrade_feature__mail_accounts_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__mail_accounts_free
        ),
        SmarttFeature(
          iconRes = R.drawable.symbol_person_24,
          tint = TINT_BLUE,
          titleRes = R.string.SmarttUpgrade_feature__priority_senders_title,
          detailRes = R.string.SmarttUpgrade_feature__priority_senders_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__not_included
        ),
        SmarttFeature(
          iconRes = R.drawable.symbol_search_24,
          tint = TINT_GREEN,
          titleRes = R.string.SmarttUpgrade_feature__mail_history_title,
          detailRes = R.string.SmarttUpgrade_feature__mail_history_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__last_30_days
        ),
        SmarttFeature(
          iconRes = R.drawable.symbol_note_24,
          tint = TINT_VIOLET,
          titleRes = R.string.SmarttUpgrade_feature__smart_digest_title,
          detailRes = R.string.SmarttUpgrade_feature__smart_digest_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__not_included
        )
      )
    ),
    SmarttFeatureGroup(
      titleRes = R.string.SmarttUpgrade_group__messaging_title,
      captionRes = R.string.SmarttUpgrade_group__messaging_caption,
      requiredPlan = SmarttPlanId.PRO,
      features = persistentListOf(
        SmarttFeature(
          iconRes = R.drawable.symbol_file_24,
          tint = TINT_PERIWINKLE,
          titleRes = R.string.SmarttUpgrade_feature__file_transfers_title,
          detailRes = R.string.SmarttUpgrade_feature__file_transfers_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__file_transfers_free
        ),
        SmarttFeature(
          iconRes = R.drawable.smartt_symbol_mic_20,
          tint = TINT_PINK,
          titleRes = R.string.SmarttUpgrade_feature__transcription_title,
          detailRes = R.string.SmarttUpgrade_feature__transcription_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__not_included
        ),
        SmarttFeature(
          iconRes = R.drawable.smartt_symbol_globe_20,
          tint = TINT_BLUE,
          titleRes = R.string.SmarttUpgrade_feature__translation_title,
          detailRes = R.string.SmarttUpgrade_feature__translation_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__translation_free
        )
      )
    ),
    SmarttFeatureGroup(
      titleRes = R.string.SmarttUpgrade_group__privacy_title,
      captionRes = R.string.SmarttUpgrade_group__privacy_caption,
      requiredPlan = SmarttPlanId.PRO,
      features = persistentListOf(
        SmarttFeature(
          iconRes = R.drawable.symbol_backup_24,
          tint = TINT_GREEN,
          titleRes = R.string.SmarttUpgrade_feature__backups_title,
          detailRes = R.string.SmarttUpgrade_feature__backups_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__last_30_days
        ),
        SmarttFeature(
          iconRes = R.drawable.symbol_lock_24,
          tint = TINT_INDIGO,
          titleRes = R.string.SmarttUpgrade_feature__granular_privacy_title,
          detailRes = R.string.SmarttUpgrade_feature__granular_privacy_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__granular_privacy_free
        ),
        SmarttFeature(
          iconRes = R.drawable.symbol_data_24,
          tint = TINT_GREY,
          titleRes = R.string.SmarttUpgrade_feature__priority_support_title,
          detailRes = R.string.SmarttUpgrade_feature__priority_support_detail,
          onFreeRes = R.string.SmarttUpgrade_feature__priority_support_free
        )
      )
    ),
    SmarttFeatureGroup(
      titleRes = R.string.SmarttUpgrade_group__parental_title,
      captionRes = R.string.SmarttUpgrade_group__parental_caption,
      requiredPlan = SmarttPlanId.PARENTAL,
      features = persistentListOf(
        SmarttFeature(
          iconRes = R.drawable.smartt_symbol_shield_20,
          tint = TINT_INDIGO,
          titleRes = R.string.SmarttUpgrade_feature__ai_filter_title,
          detailRes = R.string.SmarttUpgrade_feature__ai_filter_detail
        ),
        SmarttFeature(
          iconRes = R.drawable.symbol_camera_24,
          tint = TINT_ORANGE,
          titleRes = R.string.SmarttUpgrade_feature__outgoing_media_title,
          detailRes = R.string.SmarttUpgrade_feature__outgoing_media_detail
        ),
        SmarttFeature(
          iconRes = R.drawable.symbol_timer_24,
          tint = TINT_PERIWINKLE,
          titleRes = R.string.SmarttUpgrade_feature__child_windows_title,
          detailRes = R.string.SmarttUpgrade_feature__child_windows_detail
        ),
        SmarttFeature(
          iconRes = R.drawable.symbol_bell_slash_24,
          tint = TINT_VIOLET,
          titleRes = R.string.SmarttUpgrade_feature__bedtime_lock_title,
          detailRes = R.string.SmarttUpgrade_feature__bedtime_lock_detail
        ),
        SmarttFeature(
          iconRes = R.drawable.symbol_data_24,
          tint = TINT_GREEN,
          titleRes = R.string.SmarttUpgrade_feature__weekly_digest_title,
          detailRes = R.string.SmarttUpgrade_feature__weekly_digest_detail
        )
      )
    )
  )

  fun plan(id: SmarttPlanId): SmarttPlan = plans.first { it.id == id }

  fun parentalPlan(): SmarttPlan = plan(SmarttPlanId.PARENTAL)
}
