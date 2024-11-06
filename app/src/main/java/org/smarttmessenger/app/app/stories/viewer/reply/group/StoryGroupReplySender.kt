package com.smarttmessenger.app.stories.viewer.reply.group

import android.content.Context
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import com.smarttmessenger.app.contacts.paged.ContactSearchKey
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.database.identity.IdentityRecordList
import com.smarttmessenger.app.database.model.Mention
import com.smarttmessenger.app.database.model.ParentStoryId
import com.smarttmessenger.app.database.model.databaseprotos.BodyRangeList
import com.smarttmessenger.app.mediasend.v2.UntrustedRecords
import com.smarttmessenger.app.mms.OutgoingMessage
import com.smarttmessenger.app.sms.MessageSender

/**
 * Stateless message sender for Story Group replies and reactions.
 */
object StoryGroupReplySender {

  fun sendReply(context: Context, storyId: Long, body: CharSequence, mentions: List<Mention>, bodyRanges: BodyRangeList?): Completable {
    return sendInternal(
      context = context,
      storyId = storyId,
      body = body,
      mentions = mentions,
      bodyRanges = bodyRanges,
      isReaction = false
    )
  }

  fun sendReaction(context: Context, storyId: Long, emoji: String): Completable {
    return sendInternal(
      context = context,
      storyId = storyId,
      body = emoji,
      mentions = emptyList(),
      bodyRanges = null,
      isReaction = true
    )
  }

  private fun sendInternal(context: Context, storyId: Long, body: CharSequence, mentions: List<Mention>, bodyRanges: BodyRangeList?, isReaction: Boolean): Completable {
    val messageAndRecipient = Single.fromCallable {
      val message = SignalDatabase.messages.getMessageRecord(storyId)
      val recipient = SignalDatabase.threads.getRecipientForThreadId(message.threadId)!!

      message to recipient
    }

    return messageAndRecipient.flatMapCompletable { (message, recipient) ->
      UntrustedRecords.checkForBadIdentityRecords(setOf(ContactSearchKey.RecipientSearchKey(recipient.id, false)), System.currentTimeMillis() - IdentityRecordList.DEFAULT_UNTRUSTED_WINDOW)
        .andThen(
          Completable.create {
            MessageSender.send(
              context,
              OutgoingMessage(
                threadRecipient = recipient,
                body = body.toString(),
                sentTimeMillis = System.currentTimeMillis(),
                parentStoryId = ParentStoryId.GroupReply(message.id),
                isStoryReaction = isReaction,
                mentions = mentions,
                isSecure = true,
                bodyRanges = bodyRanges
              ),
              message.threadId,
              MessageSender.SendType.SIGNAL,
              null
            ) {
              it.onComplete()
            }
          }
        )
    }.subscribeOn(Schedulers.io())
  }
}
