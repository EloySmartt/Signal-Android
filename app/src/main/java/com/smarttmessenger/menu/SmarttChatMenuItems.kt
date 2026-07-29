/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarttmessenger.shop.ui.SmarttShopActivity
import com.smarttmessenger.upgrade.ui.SmarttUpgradeActivity
import org.signal.core.ui.compose.DropdownMenus
import org.thoughtcrime.securesms.R

/**
 * The Smartt entries at the top of the chat list overflow menu, followed by the divider that
 * separates them from the stock Signal options.
 *
 * Navigation is handled here rather than through the toolbar callback interface so that adding
 * these entries stays a one-line change at the call site.
 */
@Composable
internal fun SmarttChatMenuItems(onOptionSelected: () -> Unit) {
  val context = LocalContext.current

  DropdownMenus.Item(
    text = {
      SmarttMenuLabel(
        textRes = R.string.SmarttUpgrade__menu_item,
        iconRes = R.drawable.symbol_official_20,
        highlighted = true
      )
    },
    onClick = {
      context.startActivity(SmarttUpgradeActivity.createIntent(context))
      onOptionSelected()
    }
  )

  DropdownMenus.Item(
    text = {
      SmarttMenuLabel(
        textRes = R.string.SmarttShop__menu_item,
        iconRes = R.drawable.smartt_symbol_bag_20,
        highlighted = false
      )
    },
    onClick = {
      context.startActivity(SmarttShopActivity.createIntent(context))
      onOptionSelected()
    }
  )

  HorizontalDivider(
    color = MaterialTheme.colorScheme.outlineVariant,
    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
  )
}

@Composable
private fun SmarttMenuLabel(
  @StringRes textRes: Int,
  @DrawableRes iconRes: Int,
  highlighted: Boolean
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      tint = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(20.dp)
    )
    Text(
      text = stringResource(textRes),
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
      modifier = Modifier.padding(start = 12.dp)
    )
  }
}
