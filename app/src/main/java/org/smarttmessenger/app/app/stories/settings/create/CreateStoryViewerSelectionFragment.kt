package com.smarttmessenger.app.stories.settings.create

import androidx.navigation.fragment.findNavController
import com.smarttmessenger.app.R
import com.smarttmessenger.app.database.model.DistributionListId
import com.smarttmessenger.app.recipients.RecipientId
import com.smarttmessenger.app.stories.settings.select.BaseStoryRecipientSelectionFragment
import com.smarttmessenger.app.util.navigation.safeNavigate

/**
 * Allows user to select who will see the story they are creating
 */
class CreateStoryViewerSelectionFragment : BaseStoryRecipientSelectionFragment() {
  override val actionButtonLabel: Int = R.string.CreateStoryViewerSelectionFragment__next
  override val distributionListId: DistributionListId? = null

  override fun goToNextScreen(recipients: Set<RecipientId>) {
    findNavController().safeNavigate(CreateStoryViewerSelectionFragmentDirections.actionCreateStoryViewerSelectionToCreateStoryWithViewers(recipients.toTypedArray()))
  }
}
