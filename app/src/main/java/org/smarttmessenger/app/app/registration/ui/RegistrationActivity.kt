/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.registration.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.ActivityNavigator
import org.signal.core.util.logging.Log
import com.smarttmessenger.app.BaseActivity
import com.smarttmessenger.app.MainActivity
import com.smarttmessenger.app.R
import com.smarttmessenger.app.keyvalue.SignalStore
import com.smarttmessenger.app.lock.v2.CreateSvrPinActivity
import com.smarttmessenger.app.pin.PinRestoreActivity
import com.smarttmessenger.app.profiles.AvatarHelper
import com.smarttmessenger.app.profiles.edit.CreateProfileActivity
import com.smarttmessenger.app.recipients.Recipient
import com.smarttmessenger.app.registration.sms.SmsRetrieverReceiver
import com.smarttmessenger.app.registration.ui.restore.RemoteRestoreActivity
import com.smarttmessenger.app.util.DynamicNoActionBarTheme
import com.smarttmessenger.app.util.RemoteConfig

/**
 * Activity to hold the entire registration process.
 */
class RegistrationActivity : BaseActivity() {

  private val TAG = Log.tag(RegistrationActivity::class.java)

  private val dynamicTheme = DynamicNoActionBarTheme()
  val sharedViewModel: RegistrationViewModel by viewModels()

  private var smsRetrieverReceiver: SmsRetrieverReceiver? = null

  init {
    lifecycle.addObserver(SmsRetrieverObserver())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    dynamicTheme.onCreate(this)

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_registration_navigation_v2)

    sharedViewModel.isReregister = intent.getBooleanExtra(RE_REGISTRATION_EXTRA, false)

    sharedViewModel.checkpoint.observe(this) {
      if (it >= RegistrationCheckpoint.LOCAL_REGISTRATION_COMPLETE) {
        handleSuccessfulVerify()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    dynamicTheme.onResume(this)
  }

  private fun handleSuccessfulVerify() {
    if (SignalStore.misc.hasLinkedDevices) {
      SignalStore.misc.shouldShowLinkedDevicesReminder = sharedViewModel.isReregister
    }

    if (SignalStore.storageService.needsAccountRestore()) {
      Log.i(TAG, "Performing pin restore.")
      startActivity(Intent(this, PinRestoreActivity::class.java))
      finish()
    } else {
      val isProfileNameEmpty = Recipient.self().profileName.isEmpty
      val isAvatarEmpty = !AvatarHelper.hasAvatar(this, Recipient.self().id)
      val needsProfile = isProfileNameEmpty || isAvatarEmpty
      val needsPin = !sharedViewModel.hasPin()

      Log.i(TAG, "Pin restore flow not required. Profile name: $isProfileNameEmpty | Profile avatar: $isAvatarEmpty | Needs PIN: $needsPin")

      if (!needsProfile && !needsPin) {
        sharedViewModel.completeRegistration()
      }
      sharedViewModel.setInProgress(false)

      val startIntent = MainActivity.clearTop(this).apply {
        if (needsPin) {
          putExtra("next_intent", CreateSvrPinActivity.getIntentForPinCreate(this@RegistrationActivity))
        } else if (!SignalStore.registration.hasSkippedTransferOrRestore() && RemoteConfig.messageBackups) {
          putExtra("next_intent", RemoteRestoreActivity.getIntent(this@RegistrationActivity))
        } else if (needsProfile) {
          putExtra("next_intent", CreateProfileActivity.getIntentForUserProfile(this@RegistrationActivity))
        }
      }

      Log.d(TAG, "Launching ${startIntent.component}")
      startActivity(startIntent)
      finish()
      ActivityNavigator.applyPopAnimationsToPendingTransition(this)
    }
  }

  private inner class SmsRetrieverObserver : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
      smsRetrieverReceiver = SmsRetrieverReceiver(application)
      smsRetrieverReceiver?.registerReceiver()
    }

    override fun onDestroy(owner: LifecycleOwner) {
      smsRetrieverReceiver?.unregisterReceiver()
      smsRetrieverReceiver = null
    }
  }

  companion object {
    const val RE_REGISTRATION_EXTRA: String = "re_registration"

    @JvmStatic
    fun newIntentForNewRegistration(context: Context, originalIntent: Intent): Intent {
      return Intent(context, RegistrationActivity::class.java).apply {
        putExtra(RE_REGISTRATION_EXTRA, false)
        setData(originalIntent.data)
      }
    }

    @JvmStatic
    fun newIntentForReRegistration(context: Context): Intent {
      return Intent(context, RegistrationActivity::class.java).apply {
        putExtra(RE_REGISTRATION_EXTRA, true)
      }
    }
  }
}
