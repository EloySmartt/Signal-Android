package com.smarttmessenger.app.stories.settings.story

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import org.signal.core.util.concurrent.LifecycleDisposable
import org.signal.core.util.dp
import com.smarttmessenger.app.R
import com.smarttmessenger.app.components.DialogFragmentDisplayManager
import com.smarttmessenger.app.components.ProgressCardDialogFragment
import com.smarttmessenger.app.components.settings.DSLConfiguration
import com.smarttmessenger.app.components.settings.DSLSettingsAdapter
import com.smarttmessenger.app.components.settings.DSLSettingsFragment
import com.smarttmessenger.app.components.settings.DSLSettingsText
import com.smarttmessenger.app.components.settings.configure
import com.smarttmessenger.app.contacts.paged.ContactSearchAdapter
import com.smarttmessenger.app.contacts.paged.ContactSearchKey
import com.smarttmessenger.app.contacts.paged.ContactSearchPagedDataSourceRepository
import com.smarttmessenger.app.groups.ParcelableGroupId
import com.smarttmessenger.app.keyvalue.SignalStore
import com.smarttmessenger.app.mediasend.v2.stories.ChooseGroupStoryBottomSheet
import com.smarttmessenger.app.mediasend.v2.stories.ChooseStoryTypeBottomSheet
import com.smarttmessenger.app.stories.GroupStoryEducationSheet
import com.smarttmessenger.app.stories.dialogs.StoryDialogs
import com.smarttmessenger.app.stories.settings.create.CreateStoryFlowDialogFragment
import com.smarttmessenger.app.stories.settings.create.CreateStoryWithViewersFragment
import com.smarttmessenger.app.util.BottomSheetUtil
import com.smarttmessenger.app.util.adapter.mapping.MappingAdapter
import com.smarttmessenger.app.util.adapter.mapping.PagingMappingAdapter
import com.smarttmessenger.app.util.navigation.safeNavigate

/**
 * Allows the user to view their stories they can send to and modify settings.
 */
class StoriesPrivacySettingsFragment :
  DSLSettingsFragment(
    titleId = R.string.preferences__stories
  ),
  ChooseStoryTypeBottomSheet.Callback,
  GroupStoryEducationSheet.Callback {

  private val viewModel: StoriesPrivacySettingsViewModel by viewModels(factoryProducer = {
    StoriesPrivacySettingsViewModel.Factory(ContactSearchPagedDataSourceRepository(requireContext()))
  })

  private val lifecycleDisposable = LifecycleDisposable()
  private val progressDisplayManager = DialogFragmentDisplayManager { ProgressCardDialogFragment.create() }

  override fun createAdapters(): Array<MappingAdapter> {
    return arrayOf(DSLSettingsAdapter(), PagingMappingAdapter<ContactSearchKey>(), DSLSettingsAdapter())
  }

  override fun bindAdapters(adapter: ConcatAdapter) {
    lifecycleDisposable.bindTo(viewLifecycleOwner)

    val titleId = StoriesPrivacySettingsFragmentArgs.fromBundle(requireArguments()).titleId
    setTitle(titleId)

    val (top, middle, bottom) = adapter.adapters

    findNavController().addOnDestinationChangedListener { _, destination, _ ->
      if (destination.id == R.id.storiesPrivacySettingsFragment) {
        viewModel.pagingController.onDataInvalidated()
      }
    }

    @Suppress("UNCHECKED_CAST")
    ContactSearchAdapter.registerStoryItems(
      mappingAdapter = middle as PagingMappingAdapter<ContactSearchKey>,
      storyListener = { _, story, _ ->
        when {
          story.recipient.isMyStory -> findNavController().safeNavigate(StoriesPrivacySettingsFragmentDirections.actionStoryPrivacySettingsToMyStorySettings())
          story.recipient.isGroup -> findNavController().safeNavigate(StoriesPrivacySettingsFragmentDirections.actionStoryPrivacySettingsToGroupStorySettings(ParcelableGroupId.from(story.recipient.requireGroupId())))
          else -> findNavController().safeNavigate(StoriesPrivacySettingsFragmentDirections.actionStoryPrivacySettingsToPrivateStorySettings(story.recipient.requireDistributionListId()))
        }
      }
    )

    NewStoryItem.register(top as MappingAdapter)

    middle.setPagingController(viewModel.pagingController)

    parentFragmentManager.setFragmentResultListener(ChooseGroupStoryBottomSheet.GROUP_STORY, viewLifecycleOwner) { _, bundle ->
      val results = ChooseGroupStoryBottomSheet.ResultContract.getRecipientIds(bundle)
      viewModel.displayGroupsAsStories(results)
    }

    parentFragmentManager.setFragmentResultListener(CreateStoryWithViewersFragment.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
      viewModel.pagingController.onDataInvalidated()
    }

    lifecycleDisposable += viewModel.state.subscribe { state ->
      if (state.isUpdatingEnabledState) {
        progressDisplayManager.show(viewLifecycleOwner, childFragmentManager)
      } else {
        progressDisplayManager.hide()
      }

      top.submitList(getTopConfiguration(state).toMappingModelList())
      middle.submitList(getMiddleConfiguration(state).toMappingModelList())
      (bottom as MappingAdapter).submitList(getBottomConfiguration(state).toMappingModelList())
    }
  }

  private fun getTopConfiguration(state: StoriesPrivacySettingsState): DSLConfiguration {
    return configure {
      if (state.areStoriesEnabled) {
        space(16.dp)

        noPadTextPref(
          title = DSLSettingsText.from(
            R.string.StoriesPrivacySettingsFragment__story_updates_automatically_disappear,
            DSLSettingsText.TextAppearanceModifier(R.style.Signal_Text_BodyMedium),
            DSLSettingsText.ColorModifier(ContextCompat.getColor(requireContext(), R.color.signal_colorOnSurfaceVariant))
          )
        )

        space(20.dp)

        sectionHeaderPref(R.string.StoriesPrivacySettingsFragment__stories)

        customPref(
          NewStoryItem.Model {
            ChooseStoryTypeBottomSheet().show(childFragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
          }
        )
      } else {
        clickPref(
          title = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__turn_on_stories),
          summary = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__share_and_view),
          onClick = {
            viewModel.setStoriesEnabled(true)
          }
        )
      }
    }
  }

  private fun getMiddleConfiguration(state: StoriesPrivacySettingsState): DSLConfiguration {
    return if (state.areStoriesEnabled) {
      configure {
        ContactSearchAdapter.toMappingModelList(
          state.storyContactItems,
          emptySet(),
          null
        ).forEach {
          customPref(it)
        }
      }
    } else {
      configure { }
    }
  }

  private fun getBottomConfiguration(state: StoriesPrivacySettingsState): DSLConfiguration {
    return if (state.areStoriesEnabled) {
      configure {
        dividerPref()

        switchPref(
          title = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__view_receipts),
          summary = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__see_and_share),
          isChecked = state.areViewReceiptsEnabled,
          onClick = {
            viewModel.toggleViewReceipts()
          }
        )

        dividerPref()

        clickPref(
          title = DSLSettingsText.from(R.string.StoriesPrivacySettingsFragment__turn_off_stories),
          summary = DSLSettingsText.from(
            R.string.StoriesPrivacySettingsFragment__if_you_opt_out,
            DSLSettingsText.TextAppearanceModifier(R.style.Signal_Text_BodyMedium),
            DSLSettingsText.ColorModifier(ContextCompat.getColor(requireContext(), R.color.signal_colorOnSurfaceVariant))
          ),
          onClick = {
            StoryDialogs.disableStories(requireContext(), viewModel.userHasActiveStories) {
              viewModel.setStoriesEnabled(false)
            }
          }
        )
      }
    } else {
      configure { }
    }
  }

  override fun onGroupStoryClicked() {
    if (SignalStore.story.userHasSeenGroupStoryEducationSheet) {
      onGroupStoryEducationSheetNext()
    } else {
      GroupStoryEducationSheet().show(childFragmentManager, GroupStoryEducationSheet.KEY)
    }
  }

  override fun onNewStoryClicked() {
    CreateStoryFlowDialogFragment().show(parentFragmentManager, CreateStoryWithViewersFragment.REQUEST_KEY)
  }

  override fun onGroupStoryEducationSheetNext() {
    ChooseGroupStoryBottomSheet().show(parentFragmentManager, ChooseGroupStoryBottomSheet.GROUP_STORY)
  }
}
