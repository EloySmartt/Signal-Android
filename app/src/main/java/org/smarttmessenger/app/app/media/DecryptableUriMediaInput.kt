package com.smarttmessenger.app.media

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi
import com.smarttmessenger.app.database.SignalDatabase.Companion.attachments
import com.smarttmessenger.app.mms.PartAuthority
import com.smarttmessenger.app.mms.PartUriParser
import com.smarttmessenger.app.providers.BlobProvider
import com.smarttmessenger.app.video.interfaces.MediaInput
import com.smarttmessenger.app.video.videoconverter.mediadatasource.MediaDataSourceMediaInput
import java.io.IOException

/**
 * A media input source that is decrypted on the fly.
 */
@RequiresApi(api = 23)
object DecryptableUriMediaInput {
  @JvmStatic
  @Throws(IOException::class)
  fun createForUri(context: Context, uri: Uri): MediaInput {
    if (BlobProvider.isAuthority(uri)) {
      return MediaDataSourceMediaInput(BlobProvider.getInstance().getMediaDataSource(context, uri))
    }
    return if (PartAuthority.isLocalUri(uri)) {
      createForAttachmentUri(uri)
    } else {
      UriMediaInput(context, uri)
    }
  }

  private fun createForAttachmentUri(uri: Uri): MediaInput {
    val partId = PartUriParser(uri).partId
    if (!partId.isValid) {
      throw AssertionError()
    }
    val mediaDataSource = attachments.mediaDataSourceFor(partId, true) ?: throw AssertionError()
    return MediaDataSourceMediaInput(mediaDataSource)
  }
}
