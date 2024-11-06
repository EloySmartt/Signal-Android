package com.smarttmessenger.app.stories.viewer.page

import android.net.Uri
import com.smarttmessenger.app.attachments.Attachment
import com.smarttmessenger.app.conversation.ConversationMessage
import com.smarttmessenger.app.database.AttachmentTable
import com.smarttmessenger.app.recipients.Recipient
import com.smarttmessenger.app.util.MediaUtil
import java.util.Objects

/**
 * Each story is made up of a collection of posts
 */
data class StoryPost(
  val id: Long,
  val sender: Recipient,
  val group: Recipient?,
  val distributionList: Recipient?,
  val viewCount: Int,
  val replyCount: Int,
  val dateInMilliseconds: Long,
  val content: Content,
  val conversationMessage: ConversationMessage,
  val allowsReplies: Boolean,
  val hasSelfViewed: Boolean
) {
  sealed class Content(val uri: Uri?) {
    class AttachmentContent(val attachment: Attachment) : Content(attachment.uri) {
      override val transferState: Int = attachment.transferState

      override fun isVideo(): Boolean = MediaUtil.isVideo(attachment)

      override fun isText(): Boolean = false
    }
    class TextContent(uri: Uri, val recordId: Long, hasBody: Boolean, val length: Int) : Content(uri) {
      override val transferState: Int = if (hasBody) AttachmentTable.TRANSFER_PROGRESS_DONE else AttachmentTable.TRANSFER_PROGRESS_FAILED

      override fun isVideo(): Boolean = false

      override fun isText(): Boolean = true
    }

    abstract val transferState: Int

    abstract fun isVideo(): Boolean

    abstract fun isText(): Boolean

    override fun equals(other: Any?): Boolean {
      return other != null && other::class.java == this::class.java && other.hashCode() == hashCode()
    }

    override fun hashCode(): Int {
      return Objects.hash(uri, isVideo(), isText(), transferState)
    }
  }
}
