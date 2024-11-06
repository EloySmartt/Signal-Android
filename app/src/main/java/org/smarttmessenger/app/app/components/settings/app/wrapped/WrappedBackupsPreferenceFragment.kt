package com.smarttmessenger.app.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import com.smarttmessenger.app.R
import com.smarttmessenger.app.preferences.BackupsPreferenceFragment

class WrappedBackupsPreferenceFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.BackupsPreferenceFragment__chat_backups)
    return BackupsPreferenceFragment()
  }
}
