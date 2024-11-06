package com.smarttmessenger.app.stories.settings.custom.viewers

import com.smarttmessenger.app.R
import com.smarttmessenger.app.database.model.DistributionListId
import com.smarttmessenger.app.stories.settings.select.BaseStoryRecipientSelectionFragment

/**
 * Allows user to manage users that can view a story for a given distribution list.
 */
class AddViewersFragment : BaseStoryRecipientSelectionFragment() {
  override val actionButtonLabel: Int = R.string.HideStoryFromFragment__done
  override val distributionListId: DistributionListId
    get() = AddViewersFragmentArgs.fromBundle(requireArguments()).distributionListId
}
