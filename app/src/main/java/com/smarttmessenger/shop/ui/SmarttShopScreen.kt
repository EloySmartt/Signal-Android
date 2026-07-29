/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.shop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttmessenger.shop.model.SmarttShopCatalog
import com.smarttmessenger.shop.model.SmarttShopItem
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.Scaffolds
import org.signal.core.ui.compose.SignalPreview
import org.thoughtcrime.securesms.R

/**
 * Lists the hardware and books Smartt points people at. Every purchase happens on the partner
 * site, so each card ends in a link out rather than a checkout.
 */
@Composable
fun SmarttShopScreen(
  onNavigationClick: () -> Unit,
  onOpenLink: (String) -> Unit
) {
  Scaffolds.Settings(
    title = stringResource(R.string.SmarttShop__menu_item),
    onNavigationClick = onNavigationClick,
    navigationIcon = ImageVector.vectorResource(R.drawable.symbol_arrow_start_24),
    navigationContentDescription = stringResource(R.string.Smartt__back_content_description)
  ) { contentPadding ->
    LazyColumn(
      modifier = Modifier
        .padding(contentPadding)
        .fillMaxSize()
    ) {
      item {
        Text(
          text = stringResource(R.string.SmarttShop__intro),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING, vertical = 12.dp)
        )
      }

      itemsIndexed(SmarttShopCatalog.items) { index, item ->
        ShopItemCard(
          item = item,
          showDivider = index < SmarttShopCatalog.items.lastIndex,
          onOpenLink = onOpenLink
        )
      }

      item {
        Text(
          text = stringResource(R.string.SmarttShop__legal),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING, vertical = 20.dp)
        )
      }
    }
  }
}

@Composable
private fun ShopItemCard(item: SmarttShopItem, showDivider: Boolean, onOpenLink: (String) -> Unit) {
  Column(modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING, vertical = 14.dp)) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .fillMaxWidth()
        .height(216.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(colorResource(R.color.smartt_shop_plate))
    ) {
      Image(
        painter = painterResource(item.imageRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
      )
    }

    Row(
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 20.dp)
    ) {
      Text(
        text = stringResource(item.nameRes),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.weight(1f, fill = false)
      )
      Text(
        text = stringResource(item.priceRes),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 12.dp)
      )
    }

    Text(
      text = stringResource(item.metaRes),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 4.dp)
    )

    Text(
      text = stringResource(item.blurbRes),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(top = 10.dp)
    )

    Text(
      text = stringResource(item.actionRes),
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      textAlign = TextAlign.Center,
      fontSize = 14.5.sp,
      modifier = Modifier
        .padding(top = 18.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
        .clickable { onOpenLink(item.url) }
        .padding(vertical = 13.dp)
    )

    if (showDivider) {
      HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(top = 26.dp)
      )
    }
  }
}

private val HORIZONTAL_PADDING = 16.dp

@SignalPreview
@Composable
private fun SmarttShopScreenPreview() {
  Previews.Preview {
    SmarttShopScreen(onNavigationClick = {}, onOpenLink = {})
  }
}
