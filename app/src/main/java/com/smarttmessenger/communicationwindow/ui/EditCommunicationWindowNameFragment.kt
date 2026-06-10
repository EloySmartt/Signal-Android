package com.smarttmessenger.communicationwindow.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.smarttmessenger.communicationwindow.model.CommunicationWindow
import com.smarttmessenger.communicationwindow.repository.CommunicationWindowsRepository
import com.smarttmessenger.communicationwindow.viewmodel.CommunicationWindowDraftViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.util.BreakIteratorCompat
import org.signal.core.util.EditTextUtil
import org.signal.core.util.concurrent.LifecycleDisposable
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.emoji.EmojiUtil
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.app.notifications.profiles.models.NotificationProfileNamePreset
import org.thoughtcrime.securesms.reactions.any.ReactWithAnyEmojiBottomSheetDialogFragment
import org.thoughtcrime.securesms.util.BottomSheetUtil
import org.thoughtcrime.securesms.util.ViewUtil
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.navigation.safeNavigate
import org.thoughtcrime.securesms.util.text.AfterTextChanged
import org.thoughtcrime.securesms.util.views.CircularProgressMaterialButton

/**
 * Dual use create/edit name + emoji screen — mirrors EditNotificationProfileFragment.
 * Mode is driven by the `windowId` nav argument (NOT shared state): null = create (final wizard step,
 * persists the accumulated draft); non-null = edit that window's name/emoji and return.
 * Reuses fragment_edit_notification_profile.xml + NotificationProfileNamePreset.
 */
class EditCommunicationWindowNameFragment :
  DSLSettingsFragment(layoutId = R.layout.fragment_edit_notification_profile),
  ReactWithAnyEmojiBottomSheetDialogFragment.Callback {

  private val args: EditCommunicationWindowNameFragmentArgs by navArgs()

  private val draftVm: CommunicationWindowDraftViewModel by activityViewModels(
    factoryProducer = { CommunicationWindowDraftViewModel.Factory(requireContext().applicationContext) }
  )
  private val lifecycleDisposable = LifecycleDisposable()
  private val repository: CommunicationWindowsRepository by lazy { CommunicationWindowsRepository(requireContext()) }

  /** Non-null only in edit mode. */
  private val editingWindowId: String? get() = args.windowId?.takeIf { it.isNotEmpty() }
  private val createMode: Boolean get() = editingWindowId == null

  /** The window being edited (edit mode only), so we preserve its other fields when saving. */
  private var loadedWindow: CommunicationWindow? = null

  private var emojiView: ImageView? = null
  private var nameView: EditText? = null
  private var selectedEmoji: String = ""

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    toolbar.setNavigationOnClickListener {
      ViewUtil.hideKeyboard(requireContext(), requireView())
      requireActivity().onBackPressed()
    }

    val title: TextView = view.findViewById(R.id.edit_notification_profile_title)
    val countView: TextView = view.findViewById(R.id.edit_notification_profile_count)
    val saveButton: CircularProgressMaterialButton = view.findViewById(R.id.edit_notification_profile_save)
    val emojiView: ImageView = view.findViewById(R.id.edit_notification_profile_emoji)
    val nameView: EditText = view.findViewById(R.id.edit_notification_profile_name)
    val nameTextWrapper: TextInputLayout = view.findViewById(R.id.edit_notification_profile_name_wrapper)

    title.setText(R.string.CommunicationWindow__name_your_window)
    saveButton.setText(if (createMode) R.string.CommunicationWindow__create else R.string.EditProfileNameFragment_save)

    EditTextUtil.addGraphemeClusterLimitFilter(nameView, WINDOW_NAME_MAX_GLYPHS)
    nameView.addTextChangedListener(
      AfterTextChanged { editable: Editable ->
        presentCount(countView, editable.toString())
        nameTextWrapper.error = null
      }
    )

    emojiView.setOnClickListener {
      ReactWithAnyEmojiBottomSheetDialogFragment.createForAboutSelection()
        .show(childFragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
    }

    view.findViewById<View>(R.id.edit_notification_profile_clear).setOnClickListener {
      nameView.setText("")
      onEmojiSelectedInternal("")
    }

    lifecycleDisposable.bindTo(viewLifecycleOwner.lifecycle)

    this.nameView = nameView
    this.emojiView = emojiView

    // Prefill: from the draft (create) or from the loaded window (edit), exactly like NP's getInitialState().
    val id = editingWindowId
    if (id == null) {
      val draft = draftVm.current()
      nameView.setText(draft.name)
      onEmojiSelectedInternal(draft.emoji)
      ViewUtil.focusAndMoveCursorToEndAndOpenKeyboard(nameView)
    } else {
      lifecycleDisposable += repository.getWindow(id)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
          onNext = { window ->
            loadedWindow = window
            nameView.setText(window.name)
            onEmojiSelectedInternal(window.emoji)
            ViewUtil.focusAndMoveCursorToEndAndOpenKeyboard(nameView)
          },
          onError = { findNavController().navigateUp() }
        )
    }

    saveButton.setOnClickListener {
      val name = nameView.text?.toString()?.trim().orEmpty()
      if (TextUtils.isEmpty(name)) {
        nameTextWrapper.error = getString(R.string.CommunicationWindow__window_must_have_a_name)
        return@setOnClickListener
      }

      val save = if (createMode) {
        draftVm.update { it.copy(name = name, emoji = selectedEmoji) }
        draftVm.persist()
      } else {
        val window = loadedWindow ?: return@setOnClickListener
        repository.save(window.copy(name = name, emoji = selectedEmoji))
      }

      lifecycleDisposable += save
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { saveButton.setSpinning() }
        .doAfterTerminate { saveButton.cancelSpinning() }
        .subscribeBy(
          onSuccess = { saved ->
            ViewUtil.hideKeyboard(requireContext(), nameView)
            if (createMode) {
              findNavController().safeNavigate(
                EditCommunicationWindowNameFragmentDirections.actionEditWindowNameFragmentToWindowCreatedFragment(saved.windowId)
              )
            } else {
              findNavController().navigateUp()
            }
          }
        )
    }
  }

  override fun bindAdapter(adapter: MappingAdapter) {
    NotificationProfileNamePreset.register(adapter)

    val onClick = { preset: NotificationProfileNamePreset.Model ->
      nameView?.apply {
        setText(preset.bodyResource)
        setSelection(length(), length())
      }
      onEmojiSelectedInternal(preset.emoji)
    }

    adapter.submitList(
      listOf(
        NotificationProfileNamePreset.Model("💪", R.string.CommunicationWindow__work, onClick),
        NotificationProfileNamePreset.Model("😴", R.string.CommunicationWindow__sleep, onClick),
        NotificationProfileNamePreset.Model("📚", R.string.CommunicationWindow__study, onClick),
        NotificationProfileNamePreset.Model("👨‍👩‍👧‍👦", R.string.CommunicationWindow__family, onClick),
        NotificationProfileNamePreset.Model("⛺", R.string.CommunicationWindow__weekend, onClick),
        NotificationProfileNamePreset.Model("🏖️", R.string.CommunicationWindow__holidays, onClick)
      )
    )
  }

  override fun onReactWithAnyEmojiSelected(emoji: String) = onEmojiSelectedInternal(emoji)
  override fun onReactWithAnyEmojiDialogDismissed() = Unit

  private fun presentCount(countView: TextView, name: String) {
    val count = BreakIteratorCompat.getInstance().apply { setText(name) }.countBreaks()
    if (count >= WINDOW_NAME_LIMIT_DISPLAY_THRESHOLD) {
      countView.visibility = View.VISIBLE
      countView.text = resources.getString(R.string.EditNotificationProfileFragment__count, count, WINDOW_NAME_MAX_GLYPHS)
    } else {
      countView.visibility = View.GONE
    }
  }

  private fun onEmojiSelectedInternal(emoji: String) {
    selectedEmoji = emoji
    val drawable = EmojiUtil.convertToDrawable(requireContext(), emoji)
    if (drawable != null) {
      emojiView?.setImageDrawable(drawable)
    } else {
      emojiView?.setImageResource(R.drawable.symbol_emoji_plus_24)
      selectedEmoji = ""
    }
  }

  companion object {
    private const val WINDOW_NAME_MAX_GLYPHS = 32
    private const val WINDOW_NAME_LIMIT_DISPLAY_THRESHOLD = 22
  }
}
