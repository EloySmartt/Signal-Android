package com.smarttmessenger.app.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import com.smarttmessenger.app.R
import com.smarttmessenger.app.preferences.EditProxyFragment

class WrappedEditProxyFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.preferences_use_proxy)
    return EditProxyFragment()
  }
}
