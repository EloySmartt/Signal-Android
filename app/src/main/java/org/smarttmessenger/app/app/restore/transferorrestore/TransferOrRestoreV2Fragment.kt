/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.restore.transferorrestore

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import org.signal.core.util.logging.Log
import com.smarttmessenger.app.LoggingFragment
import com.smarttmessenger.app.R
import com.smarttmessenger.app.components.ViewBinderDelegate
import com.smarttmessenger.app.databinding.FragmentTransferRestoreV2Binding
import com.smarttmessenger.app.devicetransfer.newdevice.BackupRestorationType
import com.smarttmessenger.app.keyvalue.SignalStore
import com.smarttmessenger.app.registration.fragments.RegistrationViewDelegate
import com.smarttmessenger.app.registration.ui.restore.RemoteRestoreActivity
import com.smarttmessenger.app.restore.RestoreViewModel
import com.smarttmessenger.app.util.RemoteConfig
import com.smarttmessenger.app.util.SpanUtil
import com.smarttmessenger.app.util.navigation.safeNavigate
import com.smarttmessenger.app.util.visible

/**
 * This presents a list of options for the user to restore (or skip) a backup.
 */
class TransferOrRestoreV2Fragment : LoggingFragment(R.layout.fragment_transfer_restore_v2) {
  private val sharedViewModel by activityViewModels<RestoreViewModel>()
  private val binding: FragmentTransferRestoreV2Binding by ViewBinderDelegate(FragmentTransferRestoreV2Binding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    RegistrationViewDelegate.setDebugLogSubmitMultiTapView(binding.transferOrRestoreTitle)
    binding.transferOrRestoreFragmentTransfer.setOnClickListener { sharedViewModel.onTransferFromAndroidDeviceSelected() }
    binding.transferOrRestoreFragmentRestore.setOnClickListener { sharedViewModel.onRestoreFromLocalBackupSelected() }
    binding.transferOrRestoreFragmentRestoreRemote.setOnClickListener { sharedViewModel.onRestoreFromRemoteBackupSelected() }
    binding.transferOrRestoreFragmentNext.setOnClickListener { launchSelection(sharedViewModel.getBackupRestorationType()) }
    binding.transferOrRestoreFragmentMoreOptions.setOnClickListener {
      TransferOrRestoreMoreOptionsDialog.show(fragmentManager = childFragmentManager, skipOnly = true)
    }

    if (SignalStore.backup.backupTier == null) {
      binding.transferOrRestoreFragmentRestoreRemoteCard.visible = false
    }

    binding.transferOrRestoreFragmentRestoreRemoteCard.visible = RemoteConfig.messageBackups
    binding.transferOrRestoreFragmentMoreOptions.visible = RemoteConfig.messageBackups

    val description = getString(R.string.TransferOrRestoreFragment__transfer_your_account_and_messages_from_your_old_android_device)
    val toBold = getString(R.string.TransferOrRestoreFragment__you_need_access_to_your_old_device)

    binding.transferOrRestoreFragmentTransferDescription.text = SpanUtil.boldSubstring(description, toBold)

    sharedViewModel.uiState.observe(viewLifecycleOwner) { state ->
      updateSelection(state.restorationType)
    }

    // TODO [regv2]: port backup file detection to here
  }

  private fun updateSelection(restorationType: BackupRestorationType) {
    binding.transferOrRestoreFragmentTransferCard.isSelected = restorationType == BackupRestorationType.DEVICE_TRANSFER
    binding.transferOrRestoreFragmentRestoreCard.isSelected = restorationType == BackupRestorationType.LOCAL_BACKUP
    binding.transferOrRestoreFragmentRestoreRemoteCard.isSelected = restorationType == BackupRestorationType.REMOTE_BACKUP
  }

  private fun launchSelection(restorationType: BackupRestorationType) {
    when (restorationType) {
      BackupRestorationType.DEVICE_TRANSFER -> {
        NavHostFragment.findNavController(this).safeNavigate(TransferOrRestoreV2FragmentDirections.actionNewDeviceTransferInstructions())
      }
      BackupRestorationType.LOCAL_BACKUP -> {
        NavHostFragment.findNavController(this).safeNavigate(TransferOrRestoreV2FragmentDirections.actionTransferOrRestoreToLocalRestore())
      }
      BackupRestorationType.REMOTE_BACKUP -> {
        startActivity(RemoteRestoreActivity.getIntent(requireContext()))
      }
      else -> {
        throw IllegalArgumentException()
      }
    }
  }

  companion object {
    private val TAG = Log.tag(TransferOrRestoreV2Fragment::class.java)
  }
}
