package com.smarttmessenger.app.components.settings.conversation.permissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smarttmessenger.app.groups.GroupAccessControl
import com.smarttmessenger.app.groups.GroupId
import com.smarttmessenger.app.groups.LiveGroup
import com.smarttmessenger.app.util.SingleLiveEvent
import com.smarttmessenger.app.util.livedata.Store

class PermissionsSettingsViewModel(
  private val groupId: GroupId,
  private val repository: PermissionsSettingsRepository
) : ViewModel() {

  private val store = Store(PermissionsSettingsState())
  private val liveGroup = LiveGroup(groupId)
  private val internalEvents = SingleLiveEvent<PermissionsSettingsEvents>()

  val state: LiveData<PermissionsSettingsState> = store.stateLiveData
  val events: LiveData<PermissionsSettingsEvents> = internalEvents

  init {
    store.update(liveGroup.isSelfAdmin) { isSelfAdmin, state ->
      state.copy(selfCanEditSettings = isSelfAdmin)
    }

    store.update(liveGroup.membershipAdditionAccessControl) { membershipAdditionAccessControl, state ->
      state.copy(nonAdminCanAddMembers = membershipAdditionAccessControl == GroupAccessControl.ALL_MEMBERS)
    }

    store.update(liveGroup.attributesAccessControl) { attributesAccessControl, state ->
      state.copy(nonAdminCanEditGroupInfo = attributesAccessControl == GroupAccessControl.ALL_MEMBERS)
    }

    store.update(liveGroup.isAnnouncementGroup) { isAnnouncementGroup, state ->
      state.copy(announcementGroup = isAnnouncementGroup)
    }
  }

  fun setNonAdminCanAddMembers(nonAdminCanAddMembers: Boolean) {
    repository.applyMembershipRightsChange(groupId, nonAdminCanAddMembers.asGroupAccessControl()) { reason ->
      internalEvents.postValue(PermissionsSettingsEvents.GroupChangeError(reason))
    }
  }

  fun setNonAdminCanEditGroupInfo(nonAdminCanEditGroupInfo: Boolean) {
    repository.applyAttributesRightsChange(groupId, nonAdminCanEditGroupInfo.asGroupAccessControl()) { reason ->
      internalEvents.postValue(PermissionsSettingsEvents.GroupChangeError(reason))
    }
  }

  fun setAnnouncementGroup(announcementGroup: Boolean) {
    repository.applyAnnouncementGroupChange(groupId, announcementGroup) { reason ->
      internalEvents.postValue(PermissionsSettingsEvents.GroupChangeError(reason))
    }
  }

  private fun Boolean.asGroupAccessControl(): GroupAccessControl {
    return if (this) {
      GroupAccessControl.ALL_MEMBERS
    } else {
      GroupAccessControl.ONLY_ADMINS
    }
  }

  class Factory(
    private val groupId: GroupId,
    private val repository: PermissionsSettingsRepository
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return requireNotNull(modelClass.cast(PermissionsSettingsViewModel(groupId, repository)))
    }
  }
}
