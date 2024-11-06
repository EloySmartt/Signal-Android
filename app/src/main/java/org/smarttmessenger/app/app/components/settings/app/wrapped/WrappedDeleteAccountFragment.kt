package com.smarttmessenger.app.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import com.smarttmessenger.app.R
import com.smarttmessenger.app.delete.DeleteAccountFragment

class WrappedDeleteAccountFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.preferences__delete_account)
    return DeleteAccountFragment()
  }
}
