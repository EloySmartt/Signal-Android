/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.parental.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.signal.core.ui.compose.Rows
import org.thoughtcrime.securesms.R

/**
 * The Parental Control entry in the app settings list.
 *
 * Navigation happens here rather than through the settings callback interface so that adding this
 * row stays a one-line change at the call site. The shield is a 20dp asset shared with the upgrade
 * screen, so it is scaled rather than duplicated to match the 24dp icons of the rows around it.
 */
@Composable
internal fun SmarttParentalControlRow() {
  val context = LocalContext.current

  Rows.TextRow(
    text = stringResource(R.string.SmarttParental__title),
    icon = painterResource(R.drawable.smartt_symbol_shield_20),
    iconModifier = Modifier.size(24.dp),
    onClick = {
      context.startActivity(SmarttParentalActivity.createIntent(context))
    }
  )
}
