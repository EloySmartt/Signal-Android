package com.smarttmessenger.app.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import com.smarttmessenger.app.R
import com.smarttmessenger.app.help.HelpFragment

class WrappedHelpFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.title = getString(R.string.preferences__help)

    val fragment = HelpFragment()
    fragment.arguments = arguments

    return fragment
  }
}
