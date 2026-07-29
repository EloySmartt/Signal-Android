/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.parental.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import org.signal.core.ui.compose.theme.SignalTheme

/** Horizontal inset shared by every block on the screen. */
internal val PARENTAL_GUTTER = 16.dp

/** Alpha applied to a group of controls that the section master has switched off. */
private const val DISABLED_ALPHA = 0.4f

/**
 * The rounded surface that groups switches together. Rows must live inside one card composable so
 * the rounded corners are never split across a lazy item.
 */
@Composable
internal fun SmarttParentalCard(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = PARENTAL_GUTTER)
      .clip(RoundedCornerShape(18.dp))
      .background(SignalTheme.colors.colorSurface2)
      .padding(vertical = 4.dp)
  ) {
    content()
  }
}

/** Section title plus its explanatory caption. */
@Composable
internal fun SmarttParentalSectionHeader(title: String, caption: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = PARENTAL_GUTTER)
      .padding(top = 28.dp, bottom = 12.dp)
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = caption,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 4.dp)
    )
  }
}

/** Smaller heading used inside a section, above a control or a card. */
@Composable
internal fun SmarttParentalSubhead(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    fontWeight = FontWeight.SemiBold,
    modifier = Modifier
      .padding(horizontal = PARENTAL_GUTTER)
      .padding(top = 20.dp, bottom = 10.dp)
  )
}

/** Explanatory line that follows a segmented control or a chip group. */
@Composable
internal fun SmarttParentalHint(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier
      .padding(horizontal = PARENTAL_GUTTER)
      .padding(top = 8.dp)
  )
}

/** The tinted rounded square that carries a row's icon. */
@Composable
internal fun SmarttParentalIconTile(@DrawableRes iconRes: Int, tint: Color) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .size(34.dp)
      .clip(RoundedCornerShape(11.dp))
      .background(tint)
  ) {
    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(19.dp)
    )
  }
}

/**
 * Icon, title, description and a switch. The whole row is the toggle target.
 *
 * `Rows.ToggleRow` cannot be reused here: it takes neither an icon nor an enabled flag.
 */
@Composable
internal fun SmarttParentalSwitchRow(
  @DrawableRes iconRes: Int,
  tint: Color,
  title: String,
  detail: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  enabled: Boolean = true
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = enabled) { onCheckedChange(!checked) }
      .padding(horizontal = 14.dp, vertical = 12.dp)
  ) {
    SmarttParentalIconTile(iconRes = iconRes, tint = tint)

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 12.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = detail,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    Switch(
      checked = checked,
      onCheckedChange = null,
      enabled = enabled,
      colors = SwitchDefaults.colors(
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
        uncheckedIconColor = MaterialTheme.colorScheme.outline,
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
      )
    )
  }
}

/**
 * Dims and disables everything inside it when [enabled] is false.
 *
 * Both halves matter: the alpha communicates the state, and withholding clicks is what actually
 * stops the rows responding and marks them disabled to accessibility services.
 */
@Composable
internal fun SmarttParentalDimmed(
  enabled: Boolean,
  content: @Composable () -> Unit
) {
  Box(modifier = Modifier.alpha(if (enabled) 1f else DISABLED_ALPHA)) {
    content()
  }
}

/**
 * A pill of mutually exclusive options. Used for the three-way sensitivity and block-action
 * choices and the two-way outside-the-window choice; `weight(1f)` makes the arity irrelevant.
 */
@Composable
internal fun <T> SmarttParentalSegmentedControl(
  options: ImmutableList<T>,
  selected: T,
  label: @Composable (T) -> String,
  onOptionSelected: (T) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = PARENTAL_GUTTER)
      .clip(RoundedCornerShape(percent = 50))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(4.dp)
      .selectableGroup()
  ) {
    options.forEach { option ->
      val isSelected = option == selected

      Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .weight(1f)
          .height(38.dp)
          .clip(RoundedCornerShape(percent = 50))
          .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
          .selectable(
            selected = isSelected,
            role = Role.RadioButton,
            onClick = { onOptionSelected(option) }
          )
      ) {
        Text(
          text = label(option),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/** A read-only pill, used for the always-allowed contacts. */
@Composable
internal fun SmarttParentalChip(text: String, outlined: Boolean = false) {
  val shape = RoundedCornerShape(percent = 50)

  Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier
      .clip(shape)
      .then(
        if (outlined) {
          Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
        } else {
          Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        }
      )
      .padding(horizontal = 12.dp, vertical = 6.dp)
  )
}

/** The seven day-of-week markers on a window, Monday first, active days filled. */
@Composable
internal fun SmarttParentalDayChips(days: Set<Int>, letters: ImmutableList<Int>) {
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    letters.forEachIndexed { index, letterRes ->
      val active = index in days

      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(22.dp)
          .clip(CircleShape)
          .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Text(
          text = stringResource(letterRes),
          style = MaterialTheme.typography.labelSmall,
          color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/**
 * A row that looks like the rest of the screen but does nothing, matching the places the prototype
 * leaves unwired. Deliberately not clickable so there is no dead button.
 */
@Composable
internal fun SmarttParentalDisplayRow(
  @DrawableRes iconRes: Int,
  text: String,
  badgeCount: Int? = null
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = PARENTAL_GUTTER)
      .padding(top = 14.dp)
  ) {
    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(20.dp)
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier
        .weight(1f)
        .padding(start = 12.dp)
    )

    if (badgeCount != null) {
      Text(
        text = badgeCount.toString(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.inverseOnSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(percent = 50))
          .defaultMinSize(minWidth = 24.dp)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}
