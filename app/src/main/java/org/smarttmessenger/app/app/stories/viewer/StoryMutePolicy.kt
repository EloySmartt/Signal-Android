package com.smarttmessenger.app.stories.viewer

import com.smarttmessenger.app.util.AppForegroundObserver

/**
 * Stories are to start muted, and once unmuted, remain as such until the
 * user backgrounds the application.
 */
object StoryMutePolicy : AppForegroundObserver.Listener {
  var isContentMuted: Boolean = true

  fun initialize() {
    AppForegroundObserver.addListener(this)
  }

  override fun onBackground() {
    isContentMuted = true
  }
}
