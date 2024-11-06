package com.smarttmessenger.app.releasechannel

import com.smarttmessenger.app.attachments.Cdn
import com.smarttmessenger.app.attachments.PointerAttachment
import com.smarttmessenger.app.database.MessageTable
import com.smarttmessenger.app.database.MessageType
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.database.model.StoryType
import com.smarttmessenger.app.database.model.databaseprotos.BodyRangeList
import com.smarttmessenger.app.mms.IncomingMessage
import com.smarttmessenger.app.recipients.RecipientId
import com.smarttmessenger.app.util.MediaUtil
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentPointer
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentRemoteId
import java.util.Optional
import java.util.UUID

/**
 * One stop shop for inserting Release Channel messages.
 */
object ReleaseChannel {

  fun insertReleaseChannelMessage(
    recipientId: RecipientId,
    body: String,
    threadId: Long,
    media: String? = null,
    mediaWidth: Int = 0,
    mediaHeight: Int = 0,
    mediaType: String = "image/webp",
    mediaAttachmentUuid: UUID? = UUID.randomUUID(),
    messageRanges: BodyRangeList? = null,
    storyType: StoryType = StoryType.NONE
  ): MessageTable.InsertResult? {
    val attachments: Optional<List<SignalServiceAttachment>> = if (media != null) {
      val attachment = SignalServiceAttachmentPointer(
        cdnNumber = Cdn.S3.cdnNumber,
        remoteId = SignalServiceAttachmentRemoteId.S3,
        contentType = mediaType,
        key = null,
        size = Optional.empty(),
        preview = Optional.empty(),
        width = mediaWidth,
        height = mediaHeight,
        digest = Optional.empty(),
        incrementalDigest = Optional.empty(),
        incrementalMacChunkSize = 0,
        fileName = Optional.of(media),
        voiceNote = false,
        isBorderless = false,
        isGif = MediaUtil.isVideo(mediaType),
        caption = Optional.empty(),
        blurHash = Optional.empty(),
        uploadTimestamp = System.currentTimeMillis(),
        uuid = mediaAttachmentUuid
      )

      Optional.of(listOf(attachment))
    } else {
      Optional.empty()
    }

    val message = IncomingMessage(
      type = MessageType.NORMAL,
      from = recipientId,
      sentTimeMillis = System.currentTimeMillis(),
      serverTimeMillis = System.currentTimeMillis(),
      receivedTimeMillis = System.currentTimeMillis(),
      body = body,
      attachments = PointerAttachment.forPointers(attachments),
      serverGuid = UUID.randomUUID().toString(),
      messageRanges = messageRanges,
      storyType = storyType
    )

    return SignalDatabase.messages.insertMessageInbox(message, threadId).orElse(null)
  }
}
