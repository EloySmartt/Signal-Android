package com.smarttmessenger.app.avatar.text

import com.smarttmessenger.app.avatar.Avatar
import com.smarttmessenger.app.avatar.AvatarColorItem
import com.smarttmessenger.app.avatar.Avatars

data class TextAvatarCreationState(
  val currentAvatar: Avatar.Text
) {
  fun colors(): List<AvatarColorItem> = Avatars.colors.map { AvatarColorItem(it, currentAvatar.color == it) }
}
