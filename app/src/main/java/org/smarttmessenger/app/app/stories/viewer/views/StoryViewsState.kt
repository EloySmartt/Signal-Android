package com.smarttmessenger.app.stories.viewer.views

import com.smarttmessenger.app.recipients.Recipient

data class StoryViewsState(
  val loadState: LoadState = LoadState.INIT,
  val storyRecipient: Recipient? = null,
  val views: List<StoryViewItemData> = emptyList()
) {
  enum class LoadState {
    INIT,
    READY,
    DISABLED
  }
}
