/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.upgrade.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttmessenger.upgrade.model.SmarttBillingPeriod
import com.smarttmessenger.upgrade.model.SmarttFeature
import com.smarttmessenger.upgrade.model.SmarttFeatureGroup
import com.smarttmessenger.upgrade.model.SmarttPlan
import com.smarttmessenger.upgrade.model.SmarttPlanId
import com.smarttmessenger.upgrade.model.SmarttPricing
import com.smarttmessenger.upgrade.model.SmarttPurchase
import com.smarttmessenger.upgrade.model.SmarttUpgradeCatalog
import com.smarttmessenger.upgrade.viewmodel.SmarttUpgradeState
import org.signal.core.ui.compose.Buttons
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.Scaffolds
import org.signal.core.ui.compose.SignalPreview
import org.signal.core.ui.compose.theme.SignalTheme
import org.thoughtcrime.securesms.R

/**
 * Sells Smartt Pro and Parental Control: pick a billing period, pick a program, read what each one
 * lifts compared to the free tier.
 *
 * Purchasing is a mockup — the CTA only moves [SmarttUpgradeState.active].
 */
@Composable
fun SmarttUpgradeScreen(
  state: SmarttUpgradeState,
  onNavigationClick: () -> Unit,
  onPlanSelected: (SmarttPlanId) -> Unit,
  onPeriodSelected: (SmarttBillingPeriod) -> Unit,
  onPurchaseClick: () -> Unit,
  onRestoreClick: () -> Unit
) {
  val selectedPlan = SmarttUpgradeCatalog.plan(state.selectedPlan)

  Scaffolds.Settings(
    title = stringResource(R.string.SmarttUpgrade__menu_item),
    onNavigationClick = onNavigationClick,
    navigationIcon = ImageVector.vectorResource(R.drawable.symbol_arrow_start_24),
    navigationContentDescription = stringResource(R.string.Smartt__back_content_description)
  ) { contentPadding ->
    Column(
      modifier = Modifier
        .padding(contentPadding)
        .fillMaxSize()
    ) {
      LazyColumn(modifier = Modifier.weight(1f)) {
        item {
          UpgradeHero()
        }

        if (state.active != null) {
          item {
            ActivePlanStrip(purchase = state.active)
          }
        }

        item {
          BillingPeriodToggle(
            plan = selectedPlan,
            period = state.period,
            onPeriodSelected = onPeriodSelected
          )
        }

        items(SmarttUpgradeCatalog.plans) { plan ->
          PlanCard(
            plan = plan,
            period = state.period,
            selected = plan.id == state.selectedPlan,
            onClick = { onPlanSelected(plan.id) }
          )
        }

        items(SmarttUpgradeCatalog.featureGroups) { group ->
          FeatureGroupSection(
            group = group,
            selectedPlan = selectedPlan,
            period = state.period,
            onPlanSelected = onPlanSelected
          )
        }

        item {
          Text(
            text = stringResource(
              R.string.SmarttUpgrade_legal__body,
              SmarttUpgradeCatalog.TRIAL_DAYS,
              stringResource(R.string.app_name)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = GUTTER, vertical = 24.dp)
          )
        }
      }

      UpgradeCtaBlock(
        state = state,
        plan = selectedPlan,
        onPurchaseClick = onPurchaseClick,
        onRestoreClick = onRestoreClick
      )
    }
  }
}

@Composable
private fun UpgradeHero() {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp)
      .padding(top = 20.dp, bottom = 28.dp)
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(64.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(
          Brush.linearGradient(
            listOf(
              colorResource(R.color.smartt_pro_gradient_start),
              colorResource(R.color.smartt_pro_gradient_end)
            )
          )
        )
    ) {
      Icon(
        painter = painterResource(R.drawable.symbol_official_20),
        contentDescription = null,
        tint = colorResource(R.color.core_white),
        modifier = Modifier.size(34.dp)
      )
    }

    Text(
      text = stringResource(R.string.SmarttUpgrade_hero__title),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(top = 16.dp)
    )

    Text(
      text = stringResource(R.string.SmarttUpgrade_hero__subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 8.dp)
    )
  }
}

@Composable
private fun ActivePlanStrip(purchase: SmarttPurchase) {
  val affirmative = colorResource(R.color.smartt_pro_affirmative)
  val shortName = stringResource(SmarttUpgradeCatalog.plan(purchase.plan).shortNameRes)

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = GUTTER)
      .padding(bottom = 14.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(affirmative.copy(alpha = 0.12f))
      .padding(horizontal = 14.dp, vertical = 12.dp)
  ) {
    Icon(
      painter = painterResource(R.drawable.symbol_check_circle_24),
      contentDescription = null,
      tint = affirmative,
      modifier = Modifier.size(20.dp)
    )

    Column(modifier = Modifier.padding(start = 12.dp)) {
      Text(
        text = stringResource(R.string.SmarttUpgrade_active__title, shortName),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = stringResource(
          when (purchase.period) {
            SmarttBillingPeriod.MONTHLY -> R.string.SmarttUpgrade_active__billed_monthly
            SmarttBillingPeriod.YEARLY -> R.string.SmarttUpgrade_active__billed_yearly
          }
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun BillingPeriodToggle(
  plan: SmarttPlan,
  period: SmarttBillingPeriod,
  onPeriodSelected: (SmarttBillingPeriod) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = GUTTER)
      .clip(RoundedCornerShape(percent = 50))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(4.dp)
      .selectableGroup()
  ) {
    listOf(SmarttBillingPeriod.MONTHLY, SmarttBillingPeriod.YEARLY).forEach { entry ->
      val selected = entry == period

      Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .weight(1f)
          .height(38.dp)
          .clip(RoundedCornerShape(percent = 50))
          .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
          .selectable(
            selected = selected,
            role = Role.RadioButton,
            onClick = { onPeriodSelected(entry) }
          )
      ) {
        Text(
          text = stringResource(
            when (entry) {
              SmarttBillingPeriod.MONTHLY -> R.string.SmarttUpgrade_billing__monthly
              SmarttBillingPeriod.YEARLY -> R.string.SmarttUpgrade_billing__yearly
            }
          ),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (entry == SmarttBillingPeriod.YEARLY) {
          Text(
            text = stringResource(
              R.string.SmarttUpgrade_billing__save,
              SmarttPricing.yearlyDiscountPercent(plan)
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.core_white),
            modifier = Modifier
              .padding(start = 6.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(colorResource(R.color.smartt_pro_affirmative))
              .padding(horizontal = 5.dp, vertical = 1.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun PlanCard(
  plan: SmarttPlan,
  period: SmarttBillingPeriod,
  selected: Boolean,
  onClick: () -> Unit
) {
  val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = GUTTER)
      .padding(top = 12.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else SignalTheme.colors.colorSurface2)
      .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
      .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp)
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(plan.nameRes),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold
      )

      Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(top = 2.dp)
      ) {
        Text(
          text = SmarttPricing.format(SmarttPricing.price(plan, period)),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = stringResource(
            when (period) {
              SmarttBillingPeriod.MONTHLY -> R.string.SmarttUpgrade_plan__per_month_suffix
              SmarttBillingPeriod.YEARLY -> R.string.SmarttUpgrade_plan__per_year_suffix
            }
          ),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
        )
      }

      if (period == SmarttBillingPeriod.YEARLY) {
        Text(
          text = stringResource(
            R.string.SmarttUpgrade_plan__per_month_equivalent,
            SmarttPricing.format(SmarttPricing.perMonth(plan))
          ),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Text(
        text = stringResource(plan.taglineRes),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    RadioRing(selected = selected, modifier = Modifier.padding(start = 12.dp))
  }
}

@Composable
private fun RadioRing(selected: Boolean, modifier: Modifier = Modifier) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(22.dp)
      .border(
        width = 2.dp,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        shape = CircleShape
      )
  ) {
    if (selected) {
      Box(
        modifier = Modifier
          .size(12.dp)
          .background(MaterialTheme.colorScheme.primary, CircleShape)
      )
    }
  }
}

@Composable
private fun FeatureGroupSection(
  group: SmarttFeatureGroup,
  selectedPlan: SmarttPlan,
  period: SmarttBillingPeriod,
  onPlanSelected: (SmarttPlanId) -> Unit
) {
  // Parental Control is the only group that can be missing from a selection: everything in Pro is
  // also in Pro + Parental Control.
  val locked = group.requiredPlan == SmarttPlanId.PARENTAL && selectedPlan.id != SmarttPlanId.PARENTAL

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = GUTTER)
      .padding(top = 28.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = stringResource(group.titleRes),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )

      if (locked) {
        LockedTag(modifier = Modifier.padding(start = 8.dp))
      }
    }

    Text(
      text = if (group.captionUsesAppName) {
        stringResource(group.captionRes, stringResource(R.string.app_name))
      } else {
        stringResource(group.captionRes)
      },
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 4.dp)
    )

    Column(
      modifier = Modifier
        .padding(top = 10.dp)
        .alpha(if (locked) 0.72f else 1f)
    ) {
      group.features.forEach { feature ->
        FeatureRow(feature = feature)
      }
    }

    if (locked) {
      AddParentalControlButton(
        selectedPlan = selectedPlan,
        period = period,
        onPlanSelected = onPlanSelected
      )
    }
  }
}

@Composable
private fun FeatureRow(feature: SmarttFeature) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(34.dp)
        .clip(RoundedCornerShape(11.dp))
        .background(feature.tint)
    ) {
      Icon(
        painter = painterResource(feature.iconRes),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(19.dp)
      )
    }

    Column(modifier = Modifier.padding(start = 12.dp)) {
      Text(
        text = stringResource(feature.titleRes),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = stringResource(feature.detailRes),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )

      if (feature.onFreeRes != null) {
        Text(
          text = stringResource(R.string.SmarttUpgrade_feature__on_free, stringResource(feature.onFreeRes)),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.outline,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }
  }
}

@Composable
private fun LockedTag(modifier: Modifier = Modifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clip(RoundedCornerShape(7.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(start = 6.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
  ) {
    Icon(
      painter = painterResource(R.drawable.symbol_lock_24),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(13.dp)
    )
    Text(
      text = stringResource(R.string.SmarttUpgrade_group__not_in_pro),
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 4.dp)
    )
  }
}

@Composable
private fun AddParentalControlButton(
  selectedPlan: SmarttPlan,
  period: SmarttBillingPeriod,
  onPlanSelected: (SmarttPlanId) -> Unit
) {
  val delta = SmarttPricing.upgradeDelta(
    from = selectedPlan,
    to = SmarttUpgradeCatalog.parentalPlan(),
    period = period
  )

  Text(
    text = stringResource(
      when (period) {
        SmarttBillingPeriod.MONTHLY -> R.string.SmarttUpgrade_group__add_parental_monthly
        SmarttBillingPeriod.YEARLY -> R.string.SmarttUpgrade_group__add_parental_yearly
      },
      SmarttPricing.format(delta)
    ),
    style = MaterialTheme.typography.bodyMedium,
    fontWeight = FontWeight.SemiBold,
    color = MaterialTheme.colorScheme.primary,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .padding(top = 14.dp)
      .fillMaxWidth()
      .clip(RoundedCornerShape(22.dp))
      .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(22.dp))
      .clickable { onPlanSelected(SmarttPlanId.PARENTAL) }
      .padding(vertical = 11.dp)
  )
}

@Composable
private fun UpgradeCtaBlock(
  state: SmarttUpgradeState,
  plan: SmarttPlan,
  onPurchaseClick: () -> Unit,
  onRestoreClick: () -> Unit
) {
  val affirmative = colorResource(R.color.smartt_pro_affirmative)
  val price = SmarttPricing.price(plan, state.period)
  val shortName = stringResource(plan.shortNameRes)

  Column(modifier = Modifier.fillMaxWidth()) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

    Column(
      modifier = Modifier
        .padding(horizontal = GUTTER)
        .padding(top = 10.dp, bottom = 12.dp)
    ) {
      if (!state.isSelectionActive) {
        Text(
          text = when (state.period) {
            SmarttBillingPeriod.MONTHLY -> stringResource(
              R.string.SmarttUpgrade_cta__terms_monthly,
              SmarttUpgradeCatalog.TRIAL_DAYS,
              SmarttPricing.format(price)
            )
            SmarttBillingPeriod.YEARLY -> stringResource(
              R.string.SmarttUpgrade_cta__terms_yearly,
              SmarttUpgradeCatalog.TRIAL_DAYS,
              SmarttPricing.format(price),
              SmarttPricing.format(SmarttPricing.perMonth(plan))
            )
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
        )
      }

      Buttons.LargePrimary(
        onClick = onPurchaseClick,
        enabled = !state.isSelectionActive,
        colors = if (state.isSelectionActive) {
          ButtonDefaults.buttonColors(
            disabledContainerColor = affirmative,
            disabledContentColor = colorResource(R.color.core_white)
          )
        } else {
          ButtonDefaults.buttonColors()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Text(
          text = when {
            state.isSelectionActive -> stringResource(R.string.SmarttUpgrade_cta__active, shortName)
            state.active != null -> stringResource(
              when (state.period) {
                SmarttBillingPeriod.MONTHLY -> R.string.SmarttUpgrade_cta__switch_monthly
                SmarttBillingPeriod.YEARLY -> R.string.SmarttUpgrade_cta__switch_yearly
              },
              shortName,
              SmarttPricing.format(price)
            )
            else -> stringResource(R.string.SmarttUpgrade_cta__start_trial, SmarttUpgradeCatalog.TRIAL_DAYS)
          },
          fontSize = 15.5.sp,
          fontWeight = FontWeight.Bold
        )
      }

      TextButton(
        onClick = onRestoreClick,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(text = stringResource(R.string.SmarttUpgrade_cta__restore))
      }

      if (state.restoreAttempted) {
        Text(
          text = if (state.active == null) {
            stringResource(R.string.SmarttUpgrade_cta__restore_none)
          } else {
            stringResource(
              R.string.SmarttUpgrade_cta__restored,
              stringResource(SmarttUpgradeCatalog.plan(state.active.plan).shortNameRes)
            )
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

private val GUTTER = 16.dp

@SignalPreview
@Composable
private fun SmarttUpgradeScreenPreview() {
  Previews.Preview {
    SmarttUpgradeScreen(
      state = SmarttUpgradeState(),
      onNavigationClick = {},
      onPlanSelected = {},
      onPeriodSelected = {},
      onPurchaseClick = {},
      onRestoreClick = {}
    )
  }
}

@SignalPreview
@Composable
private fun SmarttUpgradeScreenActivePreview() {
  Previews.Preview {
    SmarttUpgradeScreen(
      state = SmarttUpgradeState(
        selectedPlan = SmarttPlanId.PARENTAL,
        period = SmarttBillingPeriod.MONTHLY,
        active = SmarttPurchase(SmarttPlanId.PARENTAL, SmarttBillingPeriod.MONTHLY),
        restoreAttempted = true
      ),
      onNavigationClick = {},
      onPlanSelected = {},
      onPeriodSelected = {},
      onPurchaseClick = {},
      onRestoreClick = {}
    )
  }
}
