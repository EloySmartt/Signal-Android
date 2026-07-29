/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.parental.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smarttmessenger.parental.model.SmarttBlockAction
import com.smarttmessenger.parental.model.SmarttChildWindow
import com.smarttmessenger.parental.model.SmarttFilterCategory
import com.smarttmessenger.parental.model.SmarttFilterSensitivity
import com.smarttmessenger.parental.model.SmarttOutsideWindow
import com.smarttmessenger.parental.model.SmarttParentalCatalog
import com.smarttmessenger.parental.model.SmarttParentalToggle
import com.smarttmessenger.parental.model.SmarttParentalToggleRow
import com.smarttmessenger.parental.viewmodel.SmarttParentalState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.thoughtcrime.securesms.R

/** Who this phone is being set up for. */
@Composable
internal fun SmarttParentalChildHeader() {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = PARENTAL_GUTTER)
      .padding(top = 16.dp)
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(52.dp)
        .clip(CircleShape)
        .background(colorResource(R.color.smartt_parental_child_avatar))
    ) {
      Text(
        text = CHILD_INITIAL,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = colorResource(R.color.core_white)
      )
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(start = 14.dp)
    ) {
      Text(
        text = stringResource(R.string.SmarttParental_header__title),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = stringResource(R.string.SmarttParental_header__subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )
    }
  }
}

/** The three headline numbers. */
@Composable
internal fun SmarttParentalStats() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = PARENTAL_GUTTER)
      .padding(top = 18.dp)
  ) {
    StatCell(
      value = SmarttParentalCatalog.BLOCKED_COUNT.toString(),
      label = stringResource(R.string.SmarttParental_stats__blocked_label)
    )
    StatCell(
      value = SmarttParentalCatalog.WAITING_COUNT.toString(),
      label = stringResource(R.string.SmarttParental_stats__waiting_label)
    )
    StatCell(
      value = stringResource(R.string.SmarttParental_stats__window_value),
      label = stringResource(R.string.SmarttParental_stats__window_label)
    )
  }
}

@Composable
private fun RowScope.StatCell(value: String, label: String) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .weight(1f)
      .semantics(mergeDescendants = true) {}
  ) {
    Text(
      text = value,
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 2.dp)
    )
  }
}

/** Master switch for the on-device filter, with its live category count. */
@Composable
internal fun SmarttParentalFilterMaster(
  state: SmarttParentalState,
  onToggleChanged: (SmarttParentalToggle, Boolean) -> Unit
) {
  SmarttParentalCard {
    SmarttParentalSwitchRow(
      iconRes = R.drawable.smartt_symbol_shield_20,
      tint = SmarttParentalCatalog.masterTint,
      title = stringResource(R.string.SmarttParental_filter__master_title),
      detail = if (state.filterOn) {
        stringResource(
          R.string.SmarttParental_filter__master_detail_on,
          state.activeCategoryCount,
          SmarttParentalCatalog.filterCategories.size
        )
      } else {
        stringResource(R.string.SmarttParental_filter__master_detail_off)
      },
      checked = state.filterOn,
      onCheckedChange = { onToggleChanged(SmarttParentalToggle.FILTER, it) }
    )
  }
}

/** The eight content categories. Only editable while the filter is on. */
@Composable
internal fun SmarttParentalCategories(
  state: SmarttParentalState,
  onCategoryChanged: (SmarttFilterCategory, Boolean) -> Unit
) {
  SmarttParentalDimmed(enabled = state.filterOn) {
    SmarttParentalCard {
      SmarttParentalCatalog.filterCategories.forEach { row ->
        SmarttParentalSwitchRow(
          iconRes = row.iconRes,
          tint = row.tint,
          title = stringResource(row.titleRes),
          detail = stringResource(row.detailRes),
          checked = row.category in state.categories,
          onCheckedChange = { onCategoryChanged(row.category, it) },
          enabled = state.filterOn
        )
      }
    }
  }
}

/** A card of switches that all share one enabled state. */
@Composable
internal fun SmarttParentalToggleCard(
  rows: ImmutableList<SmarttParentalToggleRow>,
  state: SmarttParentalState,
  onToggleChanged: (SmarttParentalToggle, Boolean) -> Unit,
  enabled: Boolean = true
) {
  SmarttParentalDimmed(enabled = enabled) {
    SmarttParentalCard {
      rows.forEach { row ->
        SmarttParentalSwitchRow(
          iconRes = row.iconRes,
          tint = row.tint,
          title = stringResource(row.titleRes),
          detail = stringResource(row.detailRes),
          checked = state.isOn(row.toggle),
          onCheckedChange = { onToggleChanged(row.toggle, it) },
          enabled = enabled
        )
      }
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

/** What the filter does with something it catches. */
@Composable
internal fun SmarttParentalBlockAction(
  state: SmarttParentalState,
  onBlockActionSelected: (SmarttBlockAction) -> Unit
) {
  SmarttParentalSubhead(stringResource(R.string.SmarttParental_filter__action_subhead))

  SmarttParentalSegmentedControl(
    options = BLOCK_ACTION_OPTIONS,
    selected = state.blockAction,
    label = { option ->
      stringResource(
        when (option) {
          SmarttBlockAction.BLUR -> R.string.SmarttParental_filter__action_blur
          SmarttBlockAction.HIDE -> R.string.SmarttParental_filter__action_hide
          SmarttBlockAction.DELETE -> R.string.SmarttParental_filter__action_delete
        }
      )
    },
    onOptionSelected = onBlockActionSelected
  )

  SmarttParentalHint(
    stringResource(
      when (state.blockAction) {
        SmarttBlockAction.BLUR -> R.string.SmarttParental_filter__action_blur_hint
        SmarttBlockAction.HIDE -> R.string.SmarttParental_filter__action_hide_hint
        SmarttBlockAction.DELETE -> R.string.SmarttParental_filter__action_delete_hint
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

/** What happens to anything that lands while every window is shut. */
@Composable
internal fun SmarttParentalOutsideWindow(
  state: SmarttParentalState,
  onOutsideWindowSelected: (SmarttOutsideWindow) -> Unit
) {
  SmarttParentalSubhead(stringResource(R.string.SmarttParental_windows__outside_subhead))

  SmarttParentalSegmentedControl(
    options = OUTSIDE_WINDOW_OPTIONS,
    selected = state.outsideWindow,
    label = { option ->
      stringResource(
        when (option) {
          SmarttOutsideWindow.HOLD -> R.string.SmarttParental_windows__outside_hold
          SmarttOutsideWindow.SILENT -> R.string.SmarttParental_windows__outside_silent
        }
      )
    },
    onOptionSelected = onOutsideWindowSelected
  )

  SmarttParentalHint(
    stringResource(
      when (state.outsideWindow) {
        SmarttOutsideWindow.HOLD -> R.string.SmarttParental_windows__outside_hold_hint
        SmarttOutsideWindow.SILENT -> R.string.SmarttParental_windows__outside_silent_hint
      }
    )
  )
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

private const val CHILD_INITIAL = "M"

private val SENSITIVITY_OPTIONS = persistentListOf(
  SmarttFilterSensitivity.LOW,
  SmarttFilterSensitivity.BALANCED,
  SmarttFilterSensitivity.STRICT
)

private val BLOCK_ACTION_OPTIONS = persistentListOf(
  SmarttBlockAction.BLUR,
  SmarttBlockAction.HIDE,
  SmarttBlockAction.DELETE
)

private val OUTSIDE_WINDOW_OPTIONS = persistentListOf(
  SmarttOutsideWindow.HOLD,
  SmarttOutsideWindow.SILENT
)
