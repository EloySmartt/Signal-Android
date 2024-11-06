/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.restore.transferorrestore

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.smarttmessenger.app.MainActivity
import com.smarttmessenger.app.components.FixedRoundedCornerBottomSheetDialogFragment
import com.smarttmessenger.app.databinding.TransferOrRestoreOptionsBottomSheetDialogFragmentBinding
import com.smarttmessenger.app.devicetransfer.newdevice.BackupRestorationType
import com.smarttmessenger.app.keyvalue.SignalStore
import com.smarttmessenger.app.profiles.edit.CreateProfileActivity
import com.smarttmessenger.app.registration.ui.restore.RemoteRestoreActivity
import com.smarttmessenger.app.restore.RestoreActivity
import com.smarttmessenger.app.util.visible

class TransferOrRestoreMoreOptionsDialog : FixedRoundedCornerBottomSheetDialogFragment() {

  override val peekHeightPercentage: Float = 1f

  private val viewModel by viewModels<TransferOrRestoreViewModel>()
  private lateinit var binding: TransferOrRestoreOptionsBottomSheetDialogFragmentBinding

  companion object {

    const val TAG = "TRANSFER_OR_RESTORE_OPTIONS_DIALOG_FRAGMENT"
    const val ARG_SKIP_ONLY = "skip_only"

    fun show(fragmentManager: FragmentManager, skipOnly: Boolean) {
      TransferOrRestoreMoreOptionsDialog().apply {
        arguments = bundleOf(ARG_SKIP_ONLY to skipOnly)
      }.show(fragmentManager, TAG)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    binding = TransferOrRestoreOptionsBottomSheetDialogFragmentBinding.inflate(inflater.cloneInContext(ContextThemeWrapper(inflater.context, themeResId)), container, false)
    if (arguments?.getBoolean(ARG_SKIP_ONLY, false) ?: false) {
      binding.transferCard.visible = false
      binding.localRestoreCard.visible = false
    }
    binding.transferOrRestoreFragmentNext.setOnClickListener { launchSelection(viewModel.getBackupRestorationType()) }
    binding.transferCard.setOnClickListener { viewModel.onTransferFromAndroidDeviceSelected() }
    binding.localRestoreCard.setOnClickListener { viewModel.onRestoreFromLocalBackupSelected() }
    binding.skipCard.setOnClickListener { viewModel.onSkipRestoreOrTransferSelected() }
    binding.cancel.setOnClickListener { dismiss() }

    viewModel.uiState.observe(viewLifecycleOwner) { state ->
      updateSelection(state.restorationType)
    }

    return binding.root
  }

  private fun launchSelection(restorationType: BackupRestorationType?) {
    when (restorationType) {
      BackupRestorationType.DEVICE_TRANSFER -> {
        startActivity(RestoreActivity.getIntentForTransfer(requireContext()))
      }
      BackupRestorationType.LOCAL_BACKUP -> {
        startActivity(RestoreActivity.getIntentForLocalRestore(requireContext()))
      }
      BackupRestorationType.REMOTE_BACKUP -> {
        startActivity(RemoteRestoreActivity.getIntent(requireContext()))
      }
      BackupRestorationType.NONE -> {
        SignalStore.registration.markSkippedTransferOrRestore()
        val startIntent = MainActivity.clearTop(requireContext()).apply {
          putExtra("next_intent", CreateProfileActivity.getIntentForUserProfile(requireContext()))
        }
        startActivity(startIntent)
      }
      else -> {
        return
      }
    }
    dismiss()
  }

  private fun updateSelection(restorationType: BackupRestorationType?) {
    binding.transferCard.isSelected = restorationType == BackupRestorationType.DEVICE_TRANSFER
    binding.localRestoreCard.isSelected = restorationType == BackupRestorationType.LOCAL_BACKUP
    binding.skipCard.isSelected = restorationType == BackupRestorationType.NONE
    binding.transferOrRestoreFragmentNext.isEnabled = restorationType != null
  }
}
