package com.smarttmessenger.communicationwindow.ui.chatlist

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.smarttmessenger.communicationwindow.model.CommunicationWindow
import com.smarttmessenger.communicationwindow.ui.CommunicationWindowsActivity
import com.smarttmessenger.communicationwindow.ui.models.CommunicationWindowPreference
import com.smarttmessenger.communicationwindow.viewmodel.CommunicationWindowSelectionViewModel
import org.signal.core.util.DimensionUnit
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.emoji.EmojiUtil
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsAdapter
import org.thoughtcrime.securesms.components.settings.DSLSettingsBottomSheetFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsIcon
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.NO_TINT
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.components.settings.conversation.preferences.LargeIconClickPreference
import org.thoughtcrime.securesms.conversation.colors.AvatarColor
import org.thoughtcrime.securesms.util.BottomSheetUtil
import org.thoughtcrime.securesms.util.formatHours
import org.thoughtcrime.securesms.util.orderOfDaysInWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * Bottom sheet that lists the user's communication windows and offers a "New communication window"
 * action — the chat-list entry point. Copied from NotificationProfileSelectionFragment.
 */
class CommunicationWindowSelectionFragment : DSLSettingsBottomSheetFragment() {

  private val viewModel: CommunicationWindowSelectionViewModel by viewModels(
    factoryProducer = { CommunicationWindowSelectionViewModel.Factory(requireContext()) }
  )

  override fun bindAdapter(adapter: DSLSettingsAdapter) {
    CommunicationWindowPreference.register(adapter)
    LargeIconClickPreference.register(adapter)

    recyclerView.itemAnimator = null

    viewModel.state.observe(viewLifecycleOwner) { windows ->
      adapter.submitList(getConfiguration(windows).toMappingModelList())
    }
  }

  private fun getConfiguration(windows: List<CommunicationWindow>): DSLConfiguration {
    return configure {
      windows.forEach { window ->
        customPref(
          CommunicationWindowPreference.Model(
            title = DSLSettingsText.from(window.name),
            summary = DSLSettingsText.from(summaryFor(window)),
            icon = if (window.emoji.isNotEmpty()) EmojiUtil.convertToDrawable(requireContext(), window.emoji)?.let { DSLSettingsIcon.from(it) } else null,
            color = colorFor(window),
            isOn = window.enabled,
            showSwitch = false,
            onClick = {
              startActivity(CommunicationWindowsActivity.newIntentForWindow(requireContext(), window.windowId))
              dismissAllowingStateLoss()
            }
          )
        )
        space(DimensionUnit.DP.toPixels(12f).toInt())
      }

      customPref(
        LargeIconClickPreference.Model(
          title = DSLSettingsText.from(R.string.CommunicationWindow__new_communication_window),
          icon = DSLSettingsIcon.from(R.drawable.new_notification_profile_pref_icon, NO_TINT),
          onClick = {
            startActivity(CommunicationWindowsActivity.newIntentForCreate(requireContext()))
            dismissAllowingStateLoss()
          }
        )
      )

      space(DimensionUnit.DP.toPixels(20f).toInt())
    }
  }

  /** "On · 9:00 AM until 5:00 PM\nMon, Tue, Wed, Thu, Fri" — matches the Figma row. */
  private fun summaryFor(window: CommunicationWindow): String {
    val status = getString(if (window.enabled) R.string.CommunicationWindow__on else R.string.CommunicationWindow__off)
    val schedule = window.schedules.firstOrNull()
    if (!window.enabled || schedule == null || !schedule.enabled) {
      return status.withSyncState(window)
    }

    val start = LocalTime.of(schedule.start / 60, schedule.start % 60).formatHours(requireContext())
    val end = LocalTime.of(schedule.end / 60, schedule.end % 60).formatHours(requireContext())
    val timeRange = getString(R.string.CommunicationWindow__s_until_s, start, end)

    val days = if (schedule.daysEnabled.size == 7) {
      getString(R.string.CommunicationWindow__every_day)
    } else {
      Locale.getDefault().orderOfDaysInWeek()
        .filter { schedule.daysEnabled.contains(it.value) }
        .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
    }

    val summary = if (days.isNotEmpty()) "$status · $timeRange\n$days" else "$status · $timeRange"
    return summary.withSyncState(window)
  }

  /**
   * Writes are local-first and pushed in the background, so a window whose push keeps failing looks
   * identical to a working one. Call it out here rather than letting it silently do nothing.
   */
  private fun String.withSyncState(window: CommunicationWindow): String =
    if (window.needsSync) "$this\n${getString(R.string.CommunicationWindow__not_synced)}" else this

  /** Deterministic background color per window (we don't store one). */
  private fun colorFor(window: CommunicationWindow): AvatarColor {
    val colors = AvatarColor.values()
    return colors[Math.floorMod(window.windowId.hashCode(), colors.size)]
  }

  companion object {
    @JvmStatic
    fun show(fragmentManager: FragmentManager) {
      CommunicationWindowSelectionFragment().show(fragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
    }
  }
}
