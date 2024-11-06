package com.smarttmessenger.app.stories.viewer.reply.group

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.paging.ObservablePagedData
import org.signal.paging.PagedData
import org.signal.paging.PagingConfig
import org.signal.paging.PagingController
import com.smarttmessenger.app.conversation.colors.GroupAuthorNameColorHelper
import com.smarttmessenger.app.conversation.colors.NameColor
import com.smarttmessenger.app.database.DatabaseObserver
import com.smarttmessenger.app.database.NoSuchMessageException
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.database.model.MessageId
import com.smarttmessenger.app.dependencies.AppDependencies
import com.smarttmessenger.app.recipients.RecipientId

class StoryGroupReplyRepository {

  fun getThreadId(storyId: Long): Single<Long> {
    return Single.fromCallable {
      SignalDatabase.messages.getThreadIdForMessage(storyId)
    }.subscribeOn(Schedulers.io())
  }

  fun getPagedReplies(parentStoryId: Long): Observable<ObservablePagedData<MessageId, ReplyBody>> {
    return getThreadId(parentStoryId)
      .toObservable()
      .flatMap { threadId ->
        Observable.create<ObservablePagedData<MessageId, ReplyBody>> { emitter ->
          val pagedData: ObservablePagedData<MessageId, ReplyBody> = PagedData.createForObservable(StoryGroupReplyDataSource(parentStoryId), PagingConfig.Builder().build())
          val controller: PagingController<MessageId> = pagedData.controller

          val updateObserver = DatabaseObserver.MessageObserver { controller.onDataItemChanged(it) }
          val insertObserver = DatabaseObserver.MessageObserver { controller.onDataItemInserted(it, PagingController.POSITION_END) }
          val conversationObserver = DatabaseObserver.Observer { controller.onDataInvalidated() }

          AppDependencies.databaseObserver.registerMessageUpdateObserver(updateObserver)
          AppDependencies.databaseObserver.registerMessageInsertObserver(threadId, insertObserver)
          AppDependencies.databaseObserver.registerConversationObserver(threadId, conversationObserver)

          emitter.setCancellable {
            AppDependencies.databaseObserver.unregisterObserver(updateObserver)
            AppDependencies.databaseObserver.unregisterObserver(insertObserver)
            AppDependencies.databaseObserver.unregisterObserver(conversationObserver)
          }

          emitter.onNext(pagedData)
        }.subscribeOn(Schedulers.io())
      }
  }

  fun getNameColorsMap(storyId: Long): Observable<Map<RecipientId, NameColor>> {
    return Single
      .fromCallable {
        try {
          val messageRecord = SignalDatabase.messages.getMessageRecord(storyId)
          val groupId = messageRecord.toRecipient.groupId.or { messageRecord.fromRecipient.groupId }
          if (groupId.isPresent) {
            GroupAuthorNameColorHelper().getColorMap(groupId.get())
          } else {
            emptyMap()
          }
        } catch (e: NoSuchMessageException) {
          emptyMap()
        }
      }
      .toObservable()
  }
}
