package com.smarttmessenger.communicationwindow.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.smarttmessenger.communicationwindow.repository.CommunicationWindowsRepository
import com.smarttmessenger.communicationwindow.viewmodel.CommunicationWindowDraftViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.concurrent.LifecycleDisposable
import org.thoughtcrime.securesms.ContactSelectionListFragment
import org.thoughtcrime.securesms.LoggingFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.ContactFilterView
import org.thoughtcrime.securesms.contacts.ContactSelectionDisplayMode
import org.thoughtcrime.securesms.contacts.paged.ChatType
import org.thoughtcrime.securesms.groups.SelectionLimits
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.ViewUtil
import org.thoughtcrime.securesms.util.views.CircularProgressMaterialButton
import java.util.Optional
import java.util.function.Consumer

/**
 * Contact picker for window exception contacts. Copied from SelectRecipientsFragment.
 *
 * Mode is driven by the `windowId` nav arg (like NP's `profileId`):
 *  - non-null  -> edit that window's exception contacts directly and save (local-first), then pop.
 *  - null      -> create wizard: write the selection into the shared draft, then pop.
 */
class SelectWindowContactsFragment : LoggingFragment(), ContactSelectionListFragment.OnContactSelectedListener {

  private val args: SelectWindowContactsFragmentArgs by navArgs()

  private val draftVm: CommunicationWindowDraftViewModel by activityViewModels(
    factoryProducer = { CommunicationWindowDraftViewModel.Factory(requireContext().applicationContext) }
  )
  private val lifecycleDisposable = LifecycleDisposable()
  private val repository: CommunicationWindowsRepository by lazy { CommunicationWindowsRepository(requireContext()) }

  private val editingWindowId: String? get() = args.windowId?.takeIf { it.isNotEmpty() }

  private val selection = LinkedHashSet<RecipientId>()
  private var addButton: CircularProgressMaterialButton? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    // Seed from the nav arg (edit) or the draft (create) — mirrors SelectRecipientsFragment.
    val current = ArrayList(args.currentSelection?.toList() ?: draftVm.exceptionRecipients.value ?: emptyList())
    selection.addAll(current)

    childFragmentManager.addFragmentOnAttachListener { _, fragment ->
      fragment.arguments = Bundle().apply {
        putInt(ContactSelectionListFragment.DISPLAY_MODE, getDefaultDisplayMode())
        putBoolean(ContactSelectionListFragment.REFRESHABLE, false)
        putBoolean(ContactSelectionListFragment.RECENTS, true)
        putParcelable(ContactSelectionListFragment.SELECTION_LIMITS, SelectionLimits.NO_LIMITS)
        putParcelableArrayList(ContactSelectionListFragment.CURRENT_SELECTION, current)
        putBoolean(ContactSelectionListFragment.HIDE_COUNT, true)
        putBoolean(ContactSelectionListFragment.DISPLAY_CHIPS, true)
        putBoolean(ContactSelectionListFragment.CAN_SELECT_SELF, false)
        putBoolean(ContactSelectionListFragment.RV_CLIP, false)
        putInt(ContactSelectionListFragment.RV_PADDING_BOTTOM, ViewUtil.dpToPx(60))
      }
    }

    return inflater.inflate(R.layout.fragment_select_recipients_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    toolbar.setTitle(R.string.CommunicationWindow__allowed_contacts)
    toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

    lifecycleDisposable.bindTo(viewLifecycleOwner.lifecycle)

    val contactFilterView: ContactFilterView = view.findViewById(R.id.contact_filter_edit_text)
    val selectionFragment = childFragmentManager.findFragmentById(R.id.contact_selection_list_fragment) as ContactSelectionListFragment

    contactFilterView.setOnFilterChangedListener {
      if (it.isNullOrEmpty()) selectionFragment.resetQueryFilter() else selectionFragment.setQueryFilter(it)
    }

    addButton = view.findViewById(R.id.select_recipients_add)
    addButton?.setOnClickListener { onAdd() }

    updateAddButton()
  }

  private fun onAdd() {
    val windowId = editingWindowId
    if (windowId == null) {
      // Create wizard: hand the selection back to the draft.
      draftVm.setExceptionRecipients(selection.toList())
      findNavController().navigateUp()
      return
    }

    // Edit mode: save the selected RecipientIds to this window directly (local-first), like NP's updateAllowedMembers.
    val ids = selection.toList()
    lifecycleDisposable += Single.fromCallable {
      repository.getLocalWindowsSync().first { it.windowId == windowId }
        .copy(exceptionContacts = ids.map { it.serialize() }.toSet())
    }
      .subscribeOn(Schedulers.io())
      .flatMap { repository.save(it) }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSubscribe { addButton?.setSpinning() }
      .doOnTerminate { addButton?.cancelSpinning() }
      .subscribeBy(onSuccess = { findNavController().navigateUp() }, onError = { findNavController().navigateUp() })
  }

  override fun onDestroyView() {
    super.onDestroyView()
    addButton = null
  }

  // Individuals only — exception matching is by sender ACI, which groups don't have.
  private fun getDefaultDisplayMode(): Int {
    return ContactSelectionDisplayMode.FLAG_PUSH or
      ContactSelectionDisplayMode.FLAG_HIDE_NEW or
      ContactSelectionDisplayMode.FLAG_HIDE_RECENT_HEADER
  }

  override fun onBeforeContactSelected(
    isFromUnknownSearchKey: Boolean,
    recipientId: Optional<RecipientId>,
    number: String?,
    chatType: Optional<ChatType>,
    callback: Consumer<Boolean>
  ) {
    if (recipientId.isPresent) {
      selection.add(recipientId.get())
      callback.accept(true)
      updateAddButton()
    } else {
      callback.accept(false)
    }
  }

  override fun onContactDeselected(recipientId: Optional<RecipientId>, number: String?, chatType: Optional<ChatType>) {
    if (recipientId.isPresent) {
      selection.remove(recipientId.get())
      updateAddButton()
    }
  }

  override fun onSelectionChanged() = Unit

  private fun updateAddButton() {
    val enabled = selection.isNotEmpty()
    addButton?.isEnabled = enabled
    addButton?.alpha = if (enabled) 1f else 0.5f
  }
}
