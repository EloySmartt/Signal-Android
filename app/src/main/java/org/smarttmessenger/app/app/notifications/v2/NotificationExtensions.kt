package com.smarttmessenger.app.notifications.v2

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.smarttmessenger.app.R
import com.smarttmessenger.app.avatar.fallback.FallbackAvatar
import com.smarttmessenger.app.avatar.fallback.FallbackAvatarDrawable
import com.smarttmessenger.app.contacts.avatars.ContactPhoto
import com.smarttmessenger.app.contacts.avatars.ProfileContactPhoto
import com.smarttmessenger.app.dependencies.AppDependencies
import com.smarttmessenger.app.mms.DecryptableStreamUriLoader.DecryptableUri
import com.smarttmessenger.app.notifications.NotificationIds
import com.smarttmessenger.app.recipients.Recipient
import com.smarttmessenger.app.util.BitmapUtil
import com.smarttmessenger.app.util.BlurTransformation
import java.util.concurrent.ExecutionException

fun Drawable?.toLargeBitmap(context: Context): Bitmap? {
  if (this == null) {
    return null
  }

  val largeIconTargetSize: Int = context.resources.getDimensionPixelSize(R.dimen.contact_photo_target_size)

  return BitmapUtil.createFromDrawable(this, largeIconTargetSize, largeIconTargetSize)
}

fun Recipient.getContactDrawable(context: Context): Drawable? {
  val contactPhoto: ContactPhoto? = if (isSelf) ProfileContactPhoto(this) else contactPhoto
  val fallbackAvatar: FallbackAvatar = if (isSelf) getFallback(context) else getFallbackAvatar()
  return if (contactPhoto != null) {
    try {
      val transforms: MutableList<Transformation<Bitmap>> = mutableListOf()
      if (shouldBlurAvatar) {
        transforms += BlurTransformation(AppDependencies.application, 0.25f, BlurTransformation.MAX_RADIUS)
      }
      transforms += CircleCrop()

      Glide.with(context.applicationContext)
        .load(contactPhoto)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .transform(MultiTransformation(transforms))
        .submit(
          context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width),
          context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
        )
        .get()
    } catch (e: InterruptedException) {
      FallbackAvatarDrawable(context, fallbackAvatar).circleCrop()
    } catch (e: ExecutionException) {
      FallbackAvatarDrawable(context, fallbackAvatar).circleCrop()
    }
  } else {
    FallbackAvatarDrawable(context, fallbackAvatar).circleCrop()
  }
}

fun Uri.toBitmap(context: Context, dimension: Int): Bitmap {
  return try {
    Glide.with(context.applicationContext)
      .asBitmap()
      .load(DecryptableUri(this))
      .diskCacheStrategy(DiskCacheStrategy.NONE)
      .submit(dimension, dimension)
      .get()
  } catch (e: InterruptedException) {
    Bitmap.createBitmap(dimension, dimension, Bitmap.Config.RGB_565)
  } catch (e: ExecutionException) {
    Bitmap.createBitmap(dimension, dimension, Bitmap.Config.RGB_565)
  }
}

fun Intent.makeUniqueToPreventMerging(): Intent {
  return setData((Uri.parse("custom://" + System.currentTimeMillis())))
}

fun Recipient.getFallback(context: Context): FallbackAvatar {
  return FallbackAvatar.forTextOrDefault(getDisplayName(context), avatarColor)
}

fun NotificationManager.isDisplayingSummaryNotification(): Boolean {
  if (Build.VERSION.SDK_INT > 23) {
    try {
      return activeNotifications.any { notification -> notification.id == NotificationIds.MESSAGE_SUMMARY }
    } catch (e: Throwable) {
    }
  }
  return false
}
