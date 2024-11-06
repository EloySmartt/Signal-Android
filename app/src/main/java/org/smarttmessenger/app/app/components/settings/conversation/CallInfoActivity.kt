package com.smarttmessenger.app.components.settings.conversation

import com.smarttmessenger.app.util.DynamicNoActionBarTheme
import com.smarttmessenger.app.util.DynamicTheme

class CallInfoActivity : ConversationSettingsActivity(), ConversationSettingsFragment.Callback {

  override val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()
}
