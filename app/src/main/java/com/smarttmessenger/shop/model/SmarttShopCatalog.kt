/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.shop.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.thoughtcrime.securesms.R

/**
 * A product on the shop screen. Checkout happens on the partner site, so an item is just copy,
 * an image and a link out.
 */
@Immutable
data class SmarttShopItem(
  @DrawableRes val imageRes: Int,
  @StringRes val nameRes: Int,
  @StringRes val priceRes: Int,
  @StringRes val metaRes: Int,
  @StringRes val blurbRes: Int,
  @StringRes val actionRes: Int,
  val url: String
)

/**
 * What the shop sells. Two partner products; nothing here is sold in-app.
 */
object SmarttShopCatalog {

  val items: ImmutableList<SmarttShopItem> = persistentListOf(
    SmarttShopItem(
      imageRes = R.drawable.smartt_shop_freedom_experiment,
      nameRes = R.string.SmarttShop_freedom_experiment__name,
      priceRes = R.string.SmarttShop_freedom_experiment__price,
      metaRes = R.string.SmarttShop_freedom_experiment__meta,
      blurbRes = R.string.SmarttShop_freedom_experiment__blurb,
      actionRes = R.string.SmarttShop_freedom_experiment__action,
      url = "https://thefreedomexperimentbook.paperform.co"
    ),
    SmarttShopItem(
      imageRes = R.drawable.smartt_shop_balance_phone,
      nameRes = R.string.SmarttShop_balance_phone__name,
      priceRes = R.string.SmarttShop_balance_phone__price,
      metaRes = R.string.SmarttShop_balance_phone__meta,
      blurbRes = R.string.SmarttShop_balance_phone__blurb,
      actionRes = R.string.SmarttShop_balance_phone__action,
      url = "https://www.thebalancephone.com/products/balance-phone"
    )
  )
}
