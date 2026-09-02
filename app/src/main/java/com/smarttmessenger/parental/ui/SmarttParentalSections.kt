/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.parental.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarttmessenger.parental.model.SmarttChildWindow
import com.smarttmessenger.parental.model.SmarttFilterCategory
import com.smarttmessenger.parental.model.SmarttFilterSensitivity
import com.smarttmessenger.parental.model.SmarttParentalCatalog
import com.smarttmessenger.parental.model.SmarttParentalToggle
import com.smarttmessenger.parental.model.SmarttParentalToggleRow
import com.smarttmessenger.parental.viewmodel.SmarttParentalState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.thoughtcrime.securesms.R

/** The eight content categories. */
@Composable
internal fun SmarttParentalCategories(
  state: SmarttParentalState,
  onCategoryChanged: (SmarttFilterCategory, Boolean) -> Unit
) {
  SmarttParentalCard {
    SmarttParentalCatalog.filterCategories.forEach { row ->
      SmarttParentalSwitchRow(
        iconRes = row.iconRes,
        tint = row.tint,
        title = stringResource(row.titleRes),
        detail = stringResource(row.detailRes),
        checked = row.category in state.categories,
        onCheckedChange = { onCategoryChanged(row.category, it) }
      )
    }
  }
}

/** A card of switches with fixed descriptions. */
@Composable
internal fun SmarttParentalToggleCard(
  rows: ImmutableList<SmarttParentalToggleRow>,
  state: SmarttParentalState,
  onToggleChanged: (SmarttParentalToggle, Boolean) -> Unit
) {
  SmarttParentalCard {
    rows.forEach { row ->
      SmarttParentalSwitchRow(
        iconRes = row.iconRes,
        tint = row.tint,
        title = stringResource(row.titleRes),
        detail = stringResource(row.detailRes),
        checked = state.isOn(row.toggle),
        onCheckedChange = { onToggleChanged(row.toggle, it) }
      )
    }
  }
}

/** Sensitivity picker plus the explanation of the current choice. */
@Composable
internal fun SmarttParentalSensitivity(
  state: SmarttParentalState,
  onSensitivitySelected: (SmarttFilterSensitivity) -> Unit
) {
  SmarttParentalSubhead(stringResource(R.string.SmarttParental_filter__sensitivity_subhead))

  SmarttParentalSegmentedControl(
    options = SENSITIVITY_OPTIONS,
    selected = state.sensitivity,
    label = { option ->
      stringResource(
        when (option) {
          SmarttFilterSensitivity.LOW -> R.string.SmarttParental_filter__sensitivity_low
          SmarttFilterSensitivity.BALANCED -> R.string.SmarttParental_filter__sensitivity_balanced
          SmarttFilterSensitivity.STRICT -> R.string.SmarttParental_filter__sensitivity_strict
        }
      )
    },
    onOptionSelected = onSensitivitySelected
  )

  SmarttParentalHint(
    stringResource(
      when (state.sensitivity) {
        SmarttFilterSensitivity.LOW -> R.string.SmarttParental_filter__sensitivity_low_hint
        SmarttFilterSensitivity.BALANCED -> R.string.SmarttParental_filter__sensitivity_balanced_hint
        SmarttFilterSensitivity.STRICT -> R.string.SmarttParental_filter__sensitivity_strict_hint
      }
    )
  )
}

/** Master switch for delivery windows, with its live window count. */
@Composable
internal fun SmarttParentalWindowsMaster(
  state: SmarttParentalState,
  onToggleChanged: (SmarttParentalToggle, Boolean) -> Unit
) {
  SmarttParentalCard {
    SmarttParentalSwitchRow(
      iconRes = R.drawable.symbol_timer_24,
      tint = SmarttParentalCatalog.masterTint,
      title = stringResource(R.string.SmarttParental_windows__master_title),
      detail = if (state.windowsOn) {
        pluralStringResource(
          R.plurals.SmarttParental_windows__master_detail_on,
          SmarttParentalCatalog.windows.size,
          SmarttParentalCatalog.windows.size
        )
      } else {
        stringResource(R.string.SmarttParental_windows__master_detail_off)
      },
      checked = state.windowsOn,
      onCheckedChange = { onToggleChanged(SmarttParentalToggle.WINDOWS, it) }
    )
  }
}

/**
 * The configured windows. Read-only: the prototype's editing affordances are omitted rather than
 * rendered as controls that do nothing.
 */
@Composable
internal fun SmarttParentalWindowList(state: SmarttParentalState) {
  SmarttParentalDimmed(enabled = state.windowsOn) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
      SmarttParentalCatalog.windows.forEach { window ->
        WindowRow(window = window)
      }
    }
  }
}

@Composable
private fun WindowRow(window: SmarttChildWindow) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = PARENTAL_GUTTER)
      .padding(vertical = 8.dp)
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(
          R.string.SmarttParental_windows__time_range,
          window.start,
          window.end
        ),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = stringResource(window.labelRes),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    if (window.days.size == SmarttParentalCatalog.dayLetters.size) {
      Text(
        text = stringResource(R.string.SmarttParental_windows__every_day),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    } else {
      SmarttParentalDayChips(
        days = window.days,
        letters = SmarttParentalCatalog.dayLetters
      )
    }
  }
}

/** Contacts that reach the child regardless of the windows. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SmarttParentalAlwaysAllowed() {
  SmarttParentalSubhead(stringResource(R.string.SmarttParental_windows__allowed_subhead))

  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = PARENTAL_GUTTER)
  ) {
    SmarttParentalCatalog.allowedContacts.forEach { contactRes ->
      SmarttParentalChip(text = stringResource(contactRes))
    }
    SmarttParentalChip(
      text = stringResource(R.string.SmarttParental_windows__allowed_add),
      outlined = true
    )
  }

  SmarttParentalHint(stringResource(R.string.SmarttParental_windows__allowed_hint))
}

private val SENSITIVITY_OPTIONS = persistentListOf(
  SmarttFilterSensitivity.LOW,
  SmarttFilterSensitivity.BALANCED,
  SmarttFilterSensitivity.STRICT
)
