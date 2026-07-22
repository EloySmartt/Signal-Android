package com.smarttmessenger.mail.ui

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.smarttmessenger.mail.model.MailProvider
import com.smarttmessenger.mail.viewmodel.MailDraftViewModel
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsIcon
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.navigation.safeNavigate

/**
 * Step 1 of the connect wizard: pick a provider. DSLSettingsFragment, same style as
 * CommunicationWindowDetailsFragment (sectionHeader + clickPref rows).
 *
 * TODO(real-auth): on click, launch the provider OAuth (Gmail: AppAuth/Google Sign-In,
 * Outlook: MSAL Android) before moving to the schedule step; put the resulting address
 * into the draft. For now the draft records the provider and continues (demo account).
 */
class ConnectMailAccountFragment : DSLSettingsFragment(titleId = R.string.Mail__connect_email) {

  private val draftVm: MailDraftViewModel by activityViewModels(
    factoryProducer = { MailDraftViewModel.Factory() }
  )

  override fun bindAdapter(adapter: MappingAdapter) {
    adapter.submitList(getConfiguration().toMappingModelList())
  }

  private fun getConfiguration(): DSLConfiguration {
    return configure {
      sectionHeaderPref(R.string.Mail__choose_a_provider)

      clickPref(
        title = DSLSettingsText.from(R.string.Mail__gmail),
        summary = DSLSettingsText.from(R.string.Mail__gmail_summary),
        icon = DSLSettingsIcon.from(R.drawable.symbol_at_24),
        onClick = { pick(MailProvider.GMAIL) }
      )

      clickPref(
        title = DSLSettingsText.from(R.string.Mail__outlook),
        summary = DSLSettingsText.from(R.string.Mail__outlook_summary),
        icon = DSLSettingsIcon.from(R.drawable.symbol_at_24),
        onClick = { pick(MailProvider.OUTLOOK) }
      )

      dividerPref()

      textPref(summary = DSLSettingsText.from(R.string.Mail__connect_privacy_note))
    }
  }

  private fun pick(provider: MailProvider) {
    draftVm.startCreate(provider)
    draftVm.update {
      it.copy(emailAddress = if (provider == MailProvider.GMAIL) "you@gmail.com" else "you@outlook.com")
    }
    findNavController().safeNavigate(R.id.action_connectMailAccountFragment_to_editMailDeliveryScheduleFragment)
  }
}
