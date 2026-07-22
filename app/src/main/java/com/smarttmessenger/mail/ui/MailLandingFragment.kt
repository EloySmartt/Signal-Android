package com.smarttmessenger.mail.ui

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.smarttmessenger.mail.model.MailMessage
import com.smarttmessenger.mail.viewmodel.MailDraftViewModel
import com.smarttmessenger.mail.viewmodel.MailLandingViewModel
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.navigation.safeNavigate

/**
 * The Mail tab content (will replace StoriesLandingFragment when the tab swap lands).
 * DSLSettingsFragment in the same style as CommunicationWindowDetailsFragment:
 * status header -> deliver-now action -> delivered messages list.
 */
class MailLandingFragment : DSLSettingsFragment(titleId = R.string.Mail__mail) {

  private val viewModel: MailLandingViewModel by activityViewModels(
    factoryProducer = { MailLandingViewModel.Factory(MailDraftViewModel.SHARED_REPOSITORY) }
  )

  override fun bindAdapter(adapter: MappingAdapter) {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      adapter.submitList(getConfiguration(state).toMappingModelList())
    }
  }

  private fun getConfiguration(state: MailLandingViewModel.State): DSLConfiguration {
    return configure {
      val account = state.account

      if (account == null) {
        textPref(
          title = DSLSettingsText.from(R.string.Mail__no_account),
          summary = DSLSettingsText.from(R.string.Mail__no_account_summary)
        )
        clickPref(
          title = DSLSettingsText.from(R.string.Mail__connect_email),
          onClick = { findNavController().safeNavigate(R.id.action_mailLandingFragment_to_connectMailAccountFragment) }
        )
        return@configure
      }

      // --- Header: account + window status (mirrors the details header row) ---
      textPref(
        title = DSLSettingsText.from(account.emailAddress),
        summary = DSLSettingsText.from(
          if (!account.deliveryWindow.scheduled) {
            getString(R.string.Mail__delivery_realtime)
          } else if (account.deliveryWindow.isOpenNow()) {
            getString(R.string.Mail__window_open)
          } else {
            getString(R.string.Mail__window_closed, state.waiting)
          }
        )
      )

      if (state.waiting > 0) {
        clickPref(
          title = DSLSettingsText.from(R.string.Mail__deliver_now),
          summary = DSLSettingsText.from(resources.getQuantityString(R.plurals.Mail__d_messages_waiting, state.waiting, state.waiting)),
          onClick = { viewModel.deliverNow() }
        )
      }

      clickPref(
        title = DSLSettingsText.from(R.string.Mail__edit_delivery_window),
        onClick = { findNavController().safeNavigate(R.id.action_mailLandingFragment_to_editMailDeliveryScheduleFragment) }
      )

      dividerPref()

      // --- Delivered messages ---
      sectionHeaderPref(R.string.Mail__inbox)
      if (state.delivered.isEmpty()) {
        textPref(summary = DSLSettingsText.from(R.string.Mail__inbox_empty))
      } else {
        state.delivered.forEach { message: MailMessage ->
          textPref(
            title = DSLSettingsText.from(message.subject),
            summary = DSLSettingsText.from(getString(R.string.Mail__from_s, message.fromName.ifEmpty { message.fromAddress }))
          )
        }
      }
    }
  }
}
