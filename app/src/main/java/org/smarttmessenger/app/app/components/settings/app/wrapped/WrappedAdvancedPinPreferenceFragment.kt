package com.smarttmessenger.app.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import com.smarttmessenger.app.R
import com.smarttmessenger.app.preferences.AdvancedPinPreferenceFragment

class WrappedAdvancedPinPreferenceFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.preferences__advanced_pin_settings)
    return AdvancedPinPreferenceFragment()
  }
}
