package com.smarttmessenger.app.components.settings.app.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.smarttmessenger.app.dependencies.AppDependencies
import com.smarttmessenger.app.keyvalue.SignalStore
import com.smarttmessenger.app.util.TextSecurePreferences
import com.smarttmessenger.app.util.livedata.Store

class AccountSettingsViewModel : ViewModel() {
  private val store: Store<AccountSettingsState> = Store(getCurrentState())

  val state: LiveData<AccountSettingsState> = store.stateLiveData

  fun refreshState() {
    store.update { getCurrentState() }
  }

  private fun getCurrentState(): AccountSettingsState {
    return AccountSettingsState(
      hasPin = SignalStore.svr.hasPin() && !SignalStore.svr.hasOptedOut(),
      pinRemindersEnabled = SignalStore.pin.arePinRemindersEnabled(),
      registrationLockEnabled = SignalStore.svr.isRegistrationLockEnabled,
      userUnregistered = TextSecurePreferences.isUnauthorizedReceived(AppDependencies.application),
      clientDeprecated = SignalStore.misc.isClientDeprecated
    )
  }
}
