/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.connectwhatsapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.signal.core.ui.compose.theme.SignalTheme
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.banner.Banner

/**
 * Persistent entry-point card for the Connect WhatsApp feature, shown at the top of the
 * conversation list. Tapping the card (or the Connect pill) opens [onConnectClick].
 *
 * Always enabled; it is registered last so that higher-priority alert banners take precedence.
 */
class ConnectWhatsAppBanner(private val onConnectClick: () -> Unit) : Banner<Unit>() {

  override val enabled: Boolean get() = true

  override val dataFlow: Flow<Unit> = flowOf(Unit)

  @Composable
  override fun DisplayBanner(model: Unit, contentPadding: PaddingValues) {
    ConnectWhatsAppCard(contentPadding = contentPadding, onConnectClick = onConnectClick)
  }
}

@Composable
private fun ConnectWhatsAppCard(contentPadding: PaddingValues, onConnectClick: () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .padding(contentPadding)
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(SignalTheme.colors.colorSurface1)
      .clickable(onClick = onConnectClick)
      .padding(horizontal = 16.dp, vertical = 14.dp)
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(colorResource(R.color.whatsapp_green))
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_whatsapp_glyph),
        contentDescription = stringResource(R.string.ConnectWhatsApp__whatsapp_icon_content_description),
        tint = colorResource(R.color.core_white),
        modifier = Modifier.size(24.dp)
      )
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 12.dp)
    ) {
      Text(
        text = stringResource(R.string.ConnectWhatsApp_card__title),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = stringResource(R.string.ConnectWhatsApp_card__subtitle, stringResource(R.string.app_name)),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Text(
      text = stringResource(R.string.ConnectWhatsApp_card__action),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.SemiBold,
      color = colorResource(R.color.core_white),
      modifier = Modifier
        .clip(RoundedCornerShape(percent = 50))
        .background(colorResource(R.color.whatsapp_green))
        .clickable(onClick = onConnectClick)
        .padding(horizontal = 18.dp, vertical = 8.dp)
    )
  }
}

@Preview
@Composable
private fun ConnectWhatsAppCardPreview() {
  SignalTheme {
    ConnectWhatsAppCard(contentPadding = PaddingValues(12.dp), onConnectClick = {})
  }
}
