/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.registration.ui.welcome

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.signal.core.util.logging.Log
import com.smarttmessenger.app.LoggingFragment
import com.smarttmessenger.app.R
import com.smarttmessenger.app.components.ViewBinderDelegate
import com.smarttmessenger.app.databinding.FragmentRegistrationWelcomeBinding
import com.smarttmessenger.app.permissions.Permissions
import com.smarttmessenger.app.registration.fragments.RegistrationViewDelegate.setDebugLogSubmitMultiTapView
import com.smarttmessenger.app.registration.fragments.WelcomePermissions
import com.smarttmessenger.app.registration.ui.RegistrationCheckpoint
import com.smarttmessenger.app.registration.ui.RegistrationViewModel
import com.smarttmessenger.app.registration.ui.grantpermissions.GrantPermissionsFragment
import com.smarttmessenger.app.restore.RestoreActivity
import com.smarttmessenger.app.util.BackupUtil
import com.smarttmessenger.app.util.CommunicationActions
import com.smarttmessenger.app.util.RemoteConfig
import com.smarttmessenger.app.util.TextSecurePreferences
import com.smarttmessenger.app.util.navigation.safeNavigate
import com.smarttmessenger.app.util.visible

/**
 * First screen that is displayed on the very first app launch.
 */
class WelcomeFragment : LoggingFragment(R.layout.fragment_registration_welcome) {
  private val sharedViewModel by activityViewModels<RegistrationViewModel>()
  private val binding: FragmentRegistrationWelcomeBinding by ViewBinderDelegate(FragmentRegistrationWelcomeBinding::bind)

  private val launchRestoreActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
    when (val resultCode = result.resultCode) {
      Activity.RESULT_OK -> {
        sharedViewModel.onBackupSuccessfullyRestored()
        findNavController().safeNavigate(WelcomeFragmentDirections.actionGoToRegistration())
      }
      Activity.RESULT_CANCELED -> {
        Log.w(TAG, "Backup restoration canceled.")
      }
      else -> Log.w(TAG, "Backup restoration activity ended with unknown result code: $resultCode")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setDebugLogSubmitMultiTapView(binding.image)
    setDebugLogSubmitMultiTapView(binding.title)
    binding.welcomeContinueButton.setOnClickListener { onContinueClicked() }
    binding.welcomeTermsButton.setOnClickListener { onTermsClicked() }
    binding.welcomeTransferOrRestore.setOnClickListener { onTransferOrRestoreClicked() }
    binding.welcomeTransferOrRestore.visible = !RemoteConfig.restoreAfterRegistration
  }

  private fun onContinueClicked() {
    TextSecurePreferences.setHasSeenWelcomeScreen(requireContext(), true)
    if (Permissions.isRuntimePermissionsRequired() && !hasAllPermissions()) {
      findNavController().safeNavigate(WelcomeFragmentDirections.actionWelcomeFragmentToGrantPermissionsFragment(GrantPermissionsFragment.WelcomeAction.CONTINUE))
    } else {
      sharedViewModel.maybePrefillE164(requireContext())
      findNavController().safeNavigate(WelcomeFragmentDirections.actionSkipRestore())
    }
  }

  private fun hasAllPermissions(): Boolean {
    val isUserSelectionRequired = BackupUtil.isUserSelectionRequired(requireContext())
    return WelcomePermissions.getWelcomePermissions(isUserSelectionRequired).all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }
  }

  private fun onTermsClicked() {
    CommunicationActions.openBrowserLink(requireContext(), TERMS_AND_CONDITIONS_URL)
  }

  private fun onTransferOrRestoreClicked() {
    if (Permissions.isRuntimePermissionsRequired() && !hasAllPermissions()) {
      findNavController().safeNavigate(WelcomeFragmentDirections.actionWelcomeFragmentToGrantPermissionsFragment(GrantPermissionsFragment.WelcomeAction.RESTORE_BACKUP))
    } else {
      sharedViewModel.setRegistrationCheckpoint(RegistrationCheckpoint.PERMISSIONS_GRANTED)

      val restoreIntent = RestoreActivity.getIntentForTransferOrRestore(requireActivity())
      launchRestoreActivity.launch(restoreIntent)
    }
  }

  companion object {
    private val TAG = Log.tag(WelcomeFragment::class.java)
    private const val TERMS_AND_CONDITIONS_URL = "https://signal.org/legal"
  }
}
