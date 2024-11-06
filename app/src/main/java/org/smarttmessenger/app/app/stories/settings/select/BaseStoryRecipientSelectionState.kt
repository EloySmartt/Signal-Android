package com.smarttmessenger.app.stories.settings.select

import com.smarttmessenger.app.database.model.DistributionListId
import com.smarttmessenger.app.database.model.DistributionListRecord
import com.smarttmessenger.app.recipients.RecipientId

data class BaseStoryRecipientSelectionState(
  val distributionListId: DistributionListId?,
  val privateStory: DistributionListRecord? = null,
  val selection: Set<RecipientId> = emptySet()
)
