package com.smarttmessenger.app.avatar.vector

import com.smarttmessenger.app.avatar.Avatar
import com.smarttmessenger.app.avatar.AvatarColorItem
import com.smarttmessenger.app.avatar.Avatars

data class VectorAvatarCreationState(
  val currentAvatar: Avatar.Vector
) {
  fun colors(): List<AvatarColorItem> = Avatars.colors.map { AvatarColorItem(it, currentAvatar.color == it) }
}
