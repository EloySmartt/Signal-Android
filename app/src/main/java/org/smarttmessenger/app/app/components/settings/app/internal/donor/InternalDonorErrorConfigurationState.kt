package com.smarttmessenger.app.components.settings.app.internal.donor

import org.signal.donations.StripeDeclineCode
import com.smarttmessenger.app.badges.models.Badge
import com.smarttmessenger.app.components.settings.app.subscription.errors.UnexpectedSubscriptionCancellation

data class InternalDonorErrorConfigurationState(
  val badges: List<Badge> = emptyList(),
  val selectedBadge: Badge? = null,
  val selectedUnexpectedSubscriptionCancellation: UnexpectedSubscriptionCancellation? = null,
  val selectedStripeDeclineCode: StripeDeclineCode.Code? = null
)
