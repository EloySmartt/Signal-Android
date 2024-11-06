package com.smarttmessenger.app.components.settings.conversation.permissions

import com.smarttmessenger.app.groups.ui.GroupChangeFailureReason

sealed class PermissionsSettingsEvents {
  class GroupChangeError(val reason: GroupChangeFailureReason) : PermissionsSettingsEvents()
}
