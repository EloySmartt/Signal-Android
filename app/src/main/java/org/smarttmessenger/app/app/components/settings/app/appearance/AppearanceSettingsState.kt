package com.smarttmessenger.app.components.settings.app.appearance

import com.smarttmessenger.app.keyvalue.SettingsValues

data class AppearanceSettingsState(
  val theme: SettingsValues.Theme,
  val messageFontSize: Int,
  val language: String,
  val isCompactNavigationBar: Boolean
)
