package com.smarttmessenger.app.components.settings.app.notifications.profiles

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.signal.core.util.concurrent.LifecycleDisposable
import com.smarttmessenger.app.R
import com.smarttmessenger.app.components.emoji.EmojiUtil
import com.smarttmessenger.app.components.settings.DSLConfiguration
import com.smarttmessenger.app.components.settings.DSLSettingsFragment
import com.smarttmessenger.app.components.settings.DSLSettingsIcon
import com.smarttmessenger.app.components.settings.DSLSettingsText
import com.smarttmessenger.app.components.settings.NO_TINT
import com.smarttmessenger.app.components.settings.app.notifications.profiles.models.NoNotificationProfiles
import com.smarttmessenger.app.components.settings.app.notifications.profiles.models.NotificationProfilePreference
import com.smarttmessenger.app.components.settings.configure
import com.smarttmessenger.app.components.settings.conversation.preferences.LargeIconClickPreference
import com.smarttmessenger.app.notifications.profiles.NotificationProfile
import com.smarttmessenger.app.notifications.profiles.NotificationProfiles
import com.smarttmessenger.app.util.adapter.mapping.MappingAdapter
import com.smarttmessenger.app.util.navigation.safeNavigate

/**
 * Primary entry point for Notification Profiles. When user has no profiles, shows empty state, otherwise shows
 * all current profiles.
 */
class NotificationProfilesFragment : DSLSettingsFragment() {

  private val viewModel: NotificationProfilesViewModel by viewModels(
    factoryProducer = { NotificationProfilesViewModel.Factory() }
  )

  private val lifecycleDisposable = LifecycleDisposable()
  private var toolbar: Toolbar? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    toolbar = view.findViewById(R.id.toolbar)

    lifecycleDisposable.bindTo(viewLifecycleOwner.lifecycle)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    toolbar = null
  }

  override fun bindAdapter(adapter: MappingAdapter) {
    NoNotificationProfiles.register(adapter)
    LargeIconClickPreference.register(adapter)
    NotificationProfilePreference.register(adapter)

    lifecycleDisposable += viewModel.getProfiles()
      .subscribe { profiles ->
        if (profiles.isEmpty()) {
          toolbar?.title = ""
        } else {
          toolbar?.setTitle(R.string.NotificationsSettingsFragment__notification_profiles)
        }
        adapter.submitList(getConfiguration(profiles).toMappingModelList())
      }
  }

  private fun getConfiguration(profiles: List<NotificationProfile>): DSLConfiguration {
    return configure {
      if (profiles.isEmpty()) {
        customPref(
          NoNotificationProfiles.Model(
            onClick = { findNavController().safeNavigate(R.id.action_notificationProfilesFragment_to_editNotificationProfileFragment) }
          )
        )
      } else {
        sectionHeaderPref(R.string.NotificationProfilesFragment__profiles)

        customPref(
          LargeIconClickPreference.Model(
            title = DSLSettingsText.from(R.string.NotificationProfilesFragment__new_profile),
            icon = DSLSettingsIcon.from(R.drawable.add_to_a_group, NO_TINT),
            onClick = { findNavController().safeNavigate(R.id.action_notificationProfilesFragment_to_editNotificationProfileFragment) }
          )
        )

        val activeProfile: NotificationProfile? = NotificationProfiles.getActiveProfile(profiles)
        profiles.sortedDescending().forEach { profile ->
          customPref(
            NotificationProfilePreference.Model(
              title = DSLSettingsText.from(profile.name),
              summary = if (profile == activeProfile) DSLSettingsText.from(NotificationProfiles.getActiveProfileDescription(requireContext(), profile)) else null,
              icon = if (profile.emoji.isNotEmpty()) EmojiUtil.convertToDrawable(requireContext(), profile.emoji)?.let { DSLSettingsIcon.from(it) } else DSLSettingsIcon.from(R.drawable.ic_moon_24, NO_TINT),
              color = profile.color,
              onClick = {
                findNavController().safeNavigate(NotificationProfilesFragmentDirections.actionNotificationProfilesFragmentToNotificationProfileDetailsFragment(profile.id))
              }
            )
          )
        }
      }
    }
  }
}
