/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.parental.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttmessenger.parental.model.SmarttFilterCategory
import com.smarttmessenger.parental.model.SmarttFilterSensitivity
import com.smarttmessenger.parental.model.SmarttParentalCatalog
import com.smarttmessenger.parental.model.SmarttParentalToggle
import com.smarttmessenger.parental.viewmodel.SmarttParentalState
import kotlinx.collections.immutable.persistentSetOf
import org.signal.core.ui.compose.Buttons
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.Scaffolds
import org.signal.core.ui.compose.SignalPreview
import org.thoughtcrime.securesms.R

/**
 * Sets up parental controls on a child's phone: the on-device media filter, the windows in which
 * messages are actually shown, who may reach the child, and the PIN that stops the child undoing
 * any of it.
 *
 * A mockup — no filter runs, no window is enforced and no PIN is ever stored or requested.
 */
@Composable
fun SmarttParentalScreen(
  state: SmarttParentalState,
  onNavigationClick: () -> Unit,
  onToggleChanged: (SmarttParentalToggle, Boolean) -> Unit,
  onCategoryChanged: (SmarttFilterCategory, Boolean) -> Unit,
  onSensitivitySelected: (SmarttFilterSensitivity) -> Unit,
  onLockClick: () -> Unit,
  onUnlockClick: () -> Unit
) {
  Scaffolds.Settings(
    title = stringResource(R.string.SmarttParental__title),
    onNavigationClick = onNavigationClick,
    navigationIcon = ImageVector.vectorResource(R.drawable.symbol_arrow_start_24),
    navigationContentDescription = stringResource(R.string.Smartt__back_content_description),
    actions = { PinChip() }
  ) { contentPadding ->
    if (state.locked) {
      LockedContent(
        onUnlockClick = onUnlockClick,
        modifier = Modifier.padding(contentPadding)
      )
    } else {
      Column(
        modifier = Modifier
          .padding(contentPadding)
          .fillMaxSize()
      ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
          item(key = "sensitivity") {
            SmarttParentalSensitivity(state = state, onSensitivitySelected = onSensitivitySelected)
          }

          item(key = "categories_head") {
            SmarttParentalSubhead(stringResource(R.string.SmarttParental_filter__categories_subhead))
          }

          item(key = "categories") {
            SmarttParentalCategories(state = state, onCategoryChanged = onCategoryChanged)
          }

          item(key = "filter_extras") {
            Box(modifier = Modifier.padding(top = 14.dp)) {
              SmarttParentalToggleCard(
                rows = SmarttParentalCatalog.filterExtraToggles,
                state = state,
                onToggleChanged = onToggleChanged
              )
            }
          }

          item(key = "review_row") {
            SmarttParentalDisplayRow(
              iconRes = R.drawable.smartt_symbol_eye_20,
              text = stringResource(R.string.SmarttParental_filter__review_row),
              badgeCount = SmarttParentalCatalog.REVIEW_COUNT
            )
          }

          item(key = "windows_head") {
            SmarttParentalSectionHeader(
              title = stringResource(R.string.SmarttParental_windows__section_title),
              caption = stringResource(R.string.SmarttParental_windows__section_caption)
            )
          }

          item(key = "windows_master") {
            SmarttParentalWindowsMaster(state = state, onToggleChanged = onToggleChanged)
          }

          item(key = "windows_list") {
            SmarttParentalWindowList(state = state)
          }

          item(key = "window_extras") {
            Box(modifier = Modifier.padding(top = 14.dp)) {
              SmarttParentalToggleCard(
                rows = SmarttParentalCatalog.windowExtraToggles,
                state = state,
                onToggleChanged = onToggleChanged
              )
            }
          }

          item(key = "always_allowed") {
            SmarttParentalAlwaysAllowed()
          }

          item(key = "contacts_head") {
            SmarttParentalSectionHeader(
              title = stringResource(R.string.SmarttParental_contacts__section_title),
              caption = stringResource(R.string.SmarttParental_contacts__section_caption)
            )
          }

          item(key = "contacts") {
            SmarttParentalToggleCard(
              rows = SmarttParentalCatalog.contactToggles,
              state = state,
              onToggleChanged = onToggleChanged
            )
          }

          item(key = "pin_head") {
            SmarttParentalSectionHeader(
              title = stringResource(R.string.SmarttParental_pin__section_title),
              caption = stringResource(R.string.SmarttParental_pin__section_caption)
            )
          }

          item(key = "pin_toggle") {
            SmarttParentalCard {
              SmarttParentalSwitchRow(
                iconRes = SmarttParentalCatalog.requirePinToggle.iconRes,
                tint = SmarttParentalCatalog.requirePinToggle.tint,
                title = stringResource(SmarttParentalCatalog.requirePinToggle.titleRes),
                detail = stringResource(SmarttParentalCatalog.requirePinToggle.detailRes),
                checked = state.isOn(SmarttParentalToggle.REQUIRE_PIN),
                onCheckedChange = { onToggleChanged(SmarttParentalToggle.REQUIRE_PIN, it) }
              )
            }
          }

          item(key = "pin_change") {
            SmarttParentalDisplayRow(
              iconRes = R.drawable.symbol_edit_24,
              text = stringResource(R.string.SmarttParental_pin__change_row)
            )
          }

          item(key = "legal") {
            Text(
              text = stringResource(
                R.string.SmarttParental_legal__body,
                stringResource(R.string.app_name)
              ),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier
                .padding(horizontal = PARENTAL_GUTTER)
                .padding(top = 28.dp, bottom = 24.dp)
            )
          }
        }

        LockFooter(onLockClick = onLockClick)
      }
    }
  }
}

@Composable
private fun PinChip() {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .padding(end = 12.dp)
      .clip(RoundedCornerShape(percent = 50))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
  ) {
    Icon(
      painter = painterResource(R.drawable.symbol_lock_24),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(14.dp)
    )
    Text(
      text = stringResource(R.string.SmarttParental__pin_chip),
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 4.dp)
    )
  }
}

@Composable
private fun LockedContent(onUnlockClick: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 32.dp)
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(64.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(SmarttParentalCatalog.masterTint)
    ) {
      Icon(
        painter = painterResource(R.drawable.symbol_lock_24),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(34.dp)
      )
    }

    Text(
      text = stringResource(R.string.SmarttParental_locked__title),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(top = 20.dp)
    )

    Text(
      text = stringResource(R.string.SmarttParental_locked__body),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 10.dp)
    )

    Buttons.LargeTonal(
      onClick = onUnlockClick,
      modifier = Modifier.padding(top = 28.dp)
    ) {
      Text(text = stringResource(R.string.SmarttParental_locked__action))
    }
  }
}

@Composable
private fun LockFooter(onLockClick: () -> Unit) {
  Column(modifier = Modifier.fillMaxWidth()) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

    Buttons.LargePrimary(
      onClick = onLockClick,
      modifier = Modifier
        .padding(horizontal = PARENTAL_GUTTER)
        .padding(top = 12.dp, bottom = 12.dp)
        .fillMaxWidth()
        .height(52.dp)
    ) {
      Icon(
        painter = painterResource(R.drawable.symbol_lock_24),
        contentDescription = null,
        modifier = Modifier.size(18.dp)
      )
      Text(
        text = stringResource(R.string.SmarttParental_footer__lock),
        fontSize = 15.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp)
      )
    }
  }
}

@SignalPreview
@Composable
private fun SmarttParentalScreenPreview() {
  Previews.Preview {
    SmarttParentalScreen(
      state = SmarttParentalState(),
      onNavigationClick = {},
      onToggleChanged = { _, _ -> },
      onCategoryChanged = { _, _ -> },
      onSensitivitySelected = {},
      onLockClick = {},
      onUnlockClick = {}
    )
  }
}

@SignalPreview
@Composable
private fun SmarttParentalScreenWindowsOffPreview() {
  Previews.Preview {
    SmarttParentalScreen(
      state = SmarttParentalState(
        toggles = persistentSetOf(SmarttParentalToggle.REQUIRE_PIN),
        sensitivity = SmarttFilterSensitivity.STRICT
      ),
      onNavigationClick = {},
      onToggleChanged = { _, _ -> },
      onCategoryChanged = { _, _ -> },
      onSensitivitySelected = {},
      onLockClick = {},
      onUnlockClick = {}
    )
  }
}

@SignalPreview
@Composable
private fun SmarttParentalScreenLockedPreview() {
  Previews.Preview {
    SmarttParentalScreen(
      state = SmarttParentalState(locked = true),
      onNavigationClick = {},
      onToggleChanged = { _, _ -> },
      onCategoryChanged = { _, _ -> },
      onSensitivitySelected = {},
      onLockClick = {},
      onUnlockClick = {}
    )
  }
}
