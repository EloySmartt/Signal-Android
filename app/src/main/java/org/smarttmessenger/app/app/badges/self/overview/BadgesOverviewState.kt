package com.smarttmessenger.app.badges.self.overview

import com.smarttmessenger.app.badges.models.Badge

data class BadgesOverviewState(
  val stage: Stage = Stage.INIT,
  val allUnlockedBadges: List<Badge> = listOf(),
  val featuredBadge: Badge? = null,
  val displayBadgesOnProfile: Boolean = false,
  val fadedBadgeId: String? = null,
  val hasInternet: Boolean = false
) {

  val hasUnexpiredBadges = allUnlockedBadges.any { it.expirationTimestamp > System.currentTimeMillis() }

  enum class Stage {
    INIT,
    READY,
    UPDATING_BADGE_DISPLAY_STATE
  }
}
