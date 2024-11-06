package com.smarttmessenger.app.stories.settings.my

import com.smarttmessenger.app.database.model.DistributionListPrivacyMode

data class MyStoryPrivacyState(val privacyMode: DistributionListPrivacyMode? = null, val connectionCount: Int = 0)
