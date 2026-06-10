package com.smarttmessenger.communicationwindow.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.smarttmessenger.communicationwindow.viewmodel.CommunicationWindowDraftViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.util.concurrent.LifecycleDisposable
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsIcon
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.app.notifications.profiles.models.NotificationProfileAddMembers
import org.thoughtcrime.securesms.components.settings.app.notifications.profiles.models.NotificationProfileRecipient
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.components.settings.conversation.preferences.RecipientPreference
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.navigation.safeNavigate
import org.thoughtcrime.securesms.util.views.CircularProgressMaterialButton

/**
 * Step 2 of the create wizard (also reused for single-screen edits from detail).
 * Reads/writes the shared draft.
 */
class AddWindowExceptionContactsFragment : DSLSettingsFragment(layoutId = R.layout.fragment_add_allowed_members) {

  private val draftVm: CommunicationWindowDraftViewModel by activityViewModels(
    factoryProducer = { CommunicationWindowDraftViewModel.Factory(requireContext().applicationContext) }
  )
  private val lifecycleDisposable = LifecycleDisposable()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    lifecycleDisposable.bindTo(viewLifecycleOwner.lifecycle)

    // The reused Signal layout hard-codes notification-profile title/description; override to Figma text.
    view.findViewById<android.widget.TextView>(R.id.edit_notification_profile_schedule_title)
      .setText(R.string.CommunicationWindow__allowed_messages)
    view.findViewById<android.widget.TextView>(R.id.edit_notification_profile_schedule_description)
      .setText(R.string.CommunicationWindow__allowed_messages_description)

    val editing = draftVm.isEditing
    val next: CircularProgressMaterialButton = view.findViewById(R.id.add_allowed_members_profile_next)
    next.setText(if (editing) R.string.EditNotificationProfileSchedule__save else R.string.CommunicationWindow__next)
    next.setOnClickListener {
      if (editing) {
        lifecycleDisposable += draftVm.persist()
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSubscribe { next.setSpinning() }
          .doAfterTerminate { next.cancelSpinning() }
          .subscribeBy(onSuccess = { findNavController().navigateUp() })
      } else {
        findNavController().safeNavigate(
          AddWindowExceptionContactsFragmentDirections.actionAddExceptionContactsFragmentToSetExpectationsFragment()
        )
      }
    }
  }

  override fun bindAdapter(adapter: MappingAdapter) {
    NotificationProfileAddMembers.register(adapter)
    NotificationProfileRecipient.register(adapter)

    val rebuild = {
      val window = draftVm.current()
      // RecipientIds resolve from cache on the main thread (same as notification profiles).
      val recipients = (draftVm.exceptionRecipients.value ?: emptyList()).map { Recipient.resolved(it) }
      adapter.submitList(
        getConfiguration(window.allowCallsFromExceptions, window.allowCallsFromAll, recipients).toMappingModelList()
      )
    }

    draftVm.draft.observe(viewLifecycleOwner) { rebuild() }
    draftVm.exceptionRecipients.observe(viewLifecycleOwner) { rebuild() }
  }

  private fun getConfiguration(
    allowCallsFromExceptions: Boolean,
    allowCallsFromAll: Boolean,
    recipients: List<Recipient>
  ): DSLConfiguration {
    return configure {
      sectionHeaderPref(R.string.CommunicationWindow__exception_contacts)

      customPref(
        NotificationProfileAddMembers.Model(
          onClick = { _, _ ->
            findNavController().safeNavigate(
              AddWindowExceptionContactsFragmentDirections.actionAddExceptionContactsFragmentToSelectWindowContactsFragment()
            )
          },
          profileId = 0L,
          currentSelection = emptySet()
        )
      )

      for (recipient in recipients) {
        customPref(
          NotificationProfileRecipient.Model(
            recipientModel = RecipientPreference.Model(recipient = recipient),
            onRemoveClick = { id -> draftVm.removeExceptionRecipient(id) }
          )
        )
      }

      sectionHeaderPref(R.string.CommunicationWindow__allowed_calls)

      switchPref(
        title = DSLSettingsText.from(R.string.CommunicationWindow__from_exception_contacts),
        icon = DSLSettingsIcon.from(R.drawable.symbol_phone_24),
        isChecked = allowCallsFromExceptions,
        onClick = { draftVm.update { it.copy(allowCallsFromExceptions = !it.allowCallsFromExceptions) } }
      )

      switchPref(
        title = DSLSettingsText.from(R.string.CommunicationWindow__from_all_contacts),
        icon = DSLSettingsIcon.from(R.drawable.symbol_phone_24),
        isChecked = allowCallsFromAll,
        onClick = { draftVm.update { it.copy(allowCallsFromAll = !it.allowCallsFromAll) } }
      )
    }
  }
}
