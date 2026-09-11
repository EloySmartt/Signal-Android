package com.smarttmessenger.communicationwindow.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.smarttmessenger.communicationwindow.model.CommunicationWindow
import com.smarttmessenger.communicationwindow.model.CommunicationWindowSchedule
import com.smarttmessenger.communicationwindow.ui.models.CommunicationWindowPreference
import com.smarttmessenger.communicationwindow.ui.models.WindowExpectationsPreference
import com.smarttmessenger.communicationwindow.viewmodel.CommunicationWindowDetailsViewModel
import com.smarttmessenger.communicationwindow.viewmodel.CommunicationWindowDraftViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.util.concurrent.LifecycleDisposable
import org.signal.core.util.concurrent.SignalExecutors
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.emoji.EmojiUtil
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsIcon
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.NO_TINT
import org.thoughtcrime.securesms.components.settings.app.notifications.profiles.models.NotificationProfileAddMembers
import org.thoughtcrime.securesms.components.settings.app.notifications.profiles.models.NotificationProfileRecipient
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.components.settings.conversation.preferences.RecipientPreference
import org.thoughtcrime.securesms.conversation.colors.AvatarColor
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.SpanUtil
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.formatHours
import org.thoughtcrime.securesms.util.navigation.safeNavigate
import org.thoughtcrime.securesms.util.orderOfDaysInWeek
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * Full read/edit view of a single window — mirrors Figma #14 and NotificationProfileDetailsFragment:
 * emoji+name+status header (pencil edits the name), schedule, exception contacts, allowed calls,
 * inline expectations chips + note, delete. Edits to chips/note/contacts/toggles persist immediately.
 */
class CommunicationWindowDetailsFragment : DSLSettingsFragment() {

  private val args: CommunicationWindowDetailsFragmentArgs by navArgs()
  private val viewModel: CommunicationWindowDetailsViewModel by viewModels(
    factoryProducer = { CommunicationWindowDetailsViewModel.Factory(args.windowId, requireContext()) }
  )
  private val draftVm: CommunicationWindowDraftViewModel by activityViewModels(
    factoryProducer = { CommunicationWindowDraftViewModel.Factory(requireContext().applicationContext) }
  )

  private val lifecycleDisposable = LifecycleDisposable()
  private var toolbar: Toolbar? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar = view.findViewById(R.id.toolbar)
    toolbar?.inflateMenu(R.menu.smartt_window_details)
    toolbar?.setOnMenuItemClickListener { item ->
      if (item.itemId == R.id.action_edit_window) {
        // Edit name/emoji: drive create-vs-edit by the windowId nav arg (mirrors NP), no shared draft.
        val window = (viewModel.state.value as? CommunicationWindowDetailsViewModel.State.Valid)?.window
        if (window != null) {
          findNavController().safeNavigate(
            CommunicationWindowDetailsFragmentDirections.actionWindowDetailsFragmentToEditWindowNameFragment().setWindowId(window.windowId)
          )
        }
        true
      } else {
        false
      }
    }
    lifecycleDisposable.bindTo(viewLifecycleOwner.lifecycle)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    toolbar = null
  }

  override fun bindAdapter(adapter: MappingAdapter) {
    CommunicationWindowPreference.register(adapter)
    NotificationProfileAddMembers.register(adapter)
    NotificationProfileRecipient.register(adapter)
    WindowExpectationsPreference.register(adapter)

    viewModel.state.observe(viewLifecycleOwner) { state ->
      when (state) {
        is CommunicationWindowDetailsViewModel.State.Valid -> {
          toolbar?.title = state.window.name
          // Exception contacts are stored as RecipientIds (like NP members). Resolve off the main thread.
          SignalExecutors.BOUNDED_IO.execute {
            val contacts = state.window.exceptionContacts.mapNotNull { serializedId ->
              runCatching { serializedId to Recipient.resolved(RecipientId.from(serializedId)) }.getOrNull()
            }
            val models = getConfiguration(state.window, contacts).toMappingModelList()
            view?.post { adapter.submitList(models) }
          }
        }
        CommunicationWindowDetailsViewModel.State.NotLoaded -> Unit
        CommunicationWindowDetailsViewModel.State.Invalid -> requireActivity().onBackPressed()
      }
    }
  }

  private fun getConfiguration(window: CommunicationWindow, contacts: List<Pair<String, Recipient>>): DSLConfiguration {
    return configure {
      // --- Header: emoji + name + "On until 5:00 PM" + master toggle ---
      customPref(
        CommunicationWindowPreference.Model(
          title = DSLSettingsText.from(window.name),
          summary = DSLSettingsText.from(headerStatus(window)),
          icon = if (window.emoji.isNotEmpty()) EmojiUtil.convertToDrawable(requireContext(), window.emoji)?.let { DSLSettingsIcon.from(it) } else DSLSettingsIcon.from(R.drawable.ic_moon_24, NO_TINT),
          color = colorFor(window),
          isOn = window.enabled,
          showSwitch = true,
          onClick = { lifecycleDisposable += viewModel.toggleEnabled().subscribe() }
        )
      )

      // Writes are local-first and pushed in the background, so a failed push is otherwise
      // invisible: the window looks saved here while the server never learned about it.
      if (window.needsSync) {
        textPref(
          title = DSLSettingsText.from(R.string.CommunicationWindow__not_synced),
          summary = DSLSettingsText.from(R.string.CommunicationWindow__not_synced_description),
          icon = DSLSettingsIcon.from(R.drawable.symbol_error_circle_24)
        )
      }

      dividerPref()

      // --- Schedule ---
      sectionHeaderPref(R.string.CommunicationWindow__edit_schedule)
      clickPref(
        title = DSLSettingsText.from(window.schedules.firstOrNull()?.describe() ?: getString(R.string.CommunicationWindow__add_a_schedule)),
        icon = DSLSettingsIcon.from(R.drawable.symbol_recent_24),
        onClick = { editSection(CommunicationWindowDetailsFragmentDirections.actionWindowDetailsFragmentToEditWindowScheduleFragment()) }
      )

      dividerPref()

      // --- Exception contacts ---
      sectionHeaderPref(R.string.CommunicationWindow__exception_contacts)
      customPref(
        NotificationProfileAddMembers.Model(
          onClick = { _, currentSelection ->
            findNavController().safeNavigate(
              CommunicationWindowDetailsFragmentDirections.actionWindowDetailsFragmentToSelectWindowContactsFragment()
                .setWindowId(window.windowId)
                .setCurrentSelection(currentSelection.toTypedArray())
            )
          },
          profileId = 0L,
          currentSelection = contacts.map { it.second.id }.toSet()
        )
      )
      for ((serializedId, recipient) in contacts) {
        customPref(
          NotificationProfileRecipient.Model(
            recipientModel = RecipientPreference.Model(recipient = recipient),
            onRemoveClick = {
              lifecycleDisposable += viewModel.removeExceptionContact(serializedId).subscribeBy(
                onComplete = {
                  view?.let { v ->
                    Snackbar.make(v, getString(R.string.CommunicationWindow__s_removed, recipient.getDisplayName(requireContext())), Snackbar.LENGTH_LONG)
                      .setAction(R.string.CommunicationWindow__undo) {
                        lifecycleDisposable += viewModel.addExceptionContact(serializedId).subscribe()
                      }
                      .show()
                  }
                }
              )
            }
          )
        )
      }

      // --- Allowed calls ---
      sectionHeaderPref(R.string.CommunicationWindow__allowed_calls)
      switchPref(
        title = DSLSettingsText.from(R.string.CommunicationWindow__from_exception_contacts),
        icon = DSLSettingsIcon.from(R.drawable.symbol_phone_24),
        isChecked = window.allowCallsFromExceptions,
        onClick = { lifecycleDisposable += viewModel.toggleAllowCallsFromExceptions().subscribe() }
      )
      switchPref(
        title = DSLSettingsText.from(R.string.CommunicationWindow__from_all_contacts),
        icon = DSLSettingsIcon.from(R.drawable.symbol_phone_24),
        isChecked = window.allowCallsFromAll,
        onClick = { lifecycleDisposable += viewModel.toggleAllowCallsFromAll().subscribe() }
      )

      dividerPref()

      // --- Expectations: check-frequency + reply-time chips + personal note (inline, persists immediately) ---
      customPref(
        WindowExpectationsPreference.Model(
          expectations = window.expectations,
          onChanged = { lifecycleDisposable += viewModel.setExpectations(it).subscribe() }
        )
      )

      dividerPref()

      // --- Delete ---
      clickPref(
        title = DSLSettingsText.from(
          R.string.CommunicationWindow__delete_window,
          ContextCompat.getColor(requireContext(), R.color.signal_alert_primary)
        ),
        icon = DSLSettingsIcon.from(R.drawable.symbol_trash_24, R.color.signal_alert_primary),
        onClick = { confirmDelete() }
      )
    }
  }

  /** Load the window into the shared draft, then navigate to the given edit destination. */
  private fun editSection(directions: NavDirections) {
    val window = (viewModel.state.value as? CommunicationWindowDetailsViewModel.State.Valid)?.window ?: return
    draftVm.startEdit(window)
    findNavController().safeNavigate(directions)
  }

  private fun confirmDelete() {
    MaterialAlertDialogBuilder(requireContext())
      .setMessage(R.string.CommunicationWindow__delete_window_confirm_message)
      .setNegativeButton(android.R.string.cancel, null)
      .setPositiveButton(
        SpanUtil.color(
          ContextCompat.getColor(requireContext(), R.color.signal_alert_primary),
          getString(R.string.CommunicationWindow__delete)
        )
      ) { _, _ ->
        // The reactive state turns Invalid once the window is gone, which leaves the screen (like NP).
        lifecycleDisposable += viewModel.deleteWindow().subscribe()
      }
      .show()
  }

  private fun headerStatus(window: CommunicationWindow): String {
    if (!window.enabled) return getString(R.string.CommunicationWindow__off)
    val schedule = window.schedules.firstOrNull()
    if (schedule == null || !schedule.enabled) return getString(R.string.CommunicationWindow__on)
    val end = LocalTime.of(schedule.end / 60, schedule.end % 60).formatHours(requireContext())
    return getString(R.string.NotificationProfileSelection__on_until_s, end)
  }

  private fun colorFor(window: CommunicationWindow): AvatarColor {
    val colors = AvatarColor.values()
    return colors[Math.floorMod(window.windowId.hashCode(), colors.size)]
  }

  private fun CommunicationWindowSchedule.describe(): String {
    if (!enabled) return getString(R.string.CommunicationWindow__off)

    val startTime = LocalTime.of(start / 60, start % 60).formatHours(requireContext())
    val endTime = LocalTime.of(end / 60, end % 60).formatHours(requireContext())

    val days = StringBuilder()
    if (daysEnabled.size == 7) {
      days.append(getString(R.string.CommunicationWindow__every_day))
    } else {
      for (day: DayOfWeek in Locale.getDefault().orderOfDaysInWeek()) {
        if (daysEnabled.contains(day.value)) {
          if (days.isNotEmpty()) days.append(", ")
          days.append(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
        }
      }
    }

    return getString(R.string.CommunicationWindow__s_until_s, startTime, endTime).let { hours ->
      if (days.isNotEmpty()) "$hours\n$days" else hours
    }
  }
}
