package com.smarttmessenger.app.notifications

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.signal.libsignal.protocol.util.Pair
import com.smarttmessenger.app.database.MessageTable.ExpirationInfo
import com.smarttmessenger.app.database.MessageTable.MarkedMessageInfo
import com.smarttmessenger.app.database.MessageTable.SyncMessageId
import com.smarttmessenger.app.database.model.MessageId
import com.smarttmessenger.app.database.model.StoryType
import com.smarttmessenger.app.dependencies.AppDependencies
import com.smarttmessenger.app.dependencies.MockApplicationDependencyProvider
import com.smarttmessenger.app.jobmanager.Job
import com.smarttmessenger.app.jobmanager.JobManager
import com.smarttmessenger.app.jobmanager.JsonJobData
import com.smarttmessenger.app.jobs.MultiDeviceReadUpdateJob
import com.smarttmessenger.app.recipients.Recipient
import com.smarttmessenger.app.recipients.RecipientId
import com.smarttmessenger.app.util.TextSecurePreferences
import java.util.LinkedList

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = Application::class)
class MarkReadReceiverTest {

  private val jobs: MutableList<Job> = LinkedList()

  @Before
  fun setUp() {
    if (!AppDependencies.isInitialized) {
      AppDependencies.init(
        ApplicationProvider.getApplicationContext(),
        MockApplicationDependencyProvider()
      )
    }

    val jobManager: JobManager = AppDependencies.jobManager
    every { jobManager.add(capture(jobs)) } returns Unit

    mockkObject(Recipient)
    every { Recipient.self() } returns Recipient()

    mockkStatic(TextSecurePreferences::class)
    every { TextSecurePreferences.isReadReceiptsEnabled(any()) } returns true
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun givenMultipleThreadsWithMultipleMessagesEach_whenIProcess_thenIProperlyGroupByThreadAndRecipient() {
    // GIVEN
    val recipients = (1L until 4L).map { id -> RecipientId.from(id) }
    val threads = (4L until 7L).toList()
    val expected = recipients.size * threads.size + 1
    val infoList = threads.map { threadId -> recipients.map { recipientId -> createMarkedMessageInfo(threadId, recipientId) } }.flatten()

    // WHEN
    MarkReadReceiver.process(infoList + infoList)

    // THEN
    Assert.assertEquals("Should have 10 total jobs, including MultiDeviceReadUpdateJob", expected.toLong(), jobs.size.toLong())

    val threadRecipientPairs: MutableSet<Pair<Long, String>> = HashSet()
    jobs.forEach { job ->
      if (job is MultiDeviceReadUpdateJob) {
        return@forEach
      }
      val data = JsonJobData.deserialize(job.serialize())

      val threadId = data.getLong("thread")
      val recipientId = data.getString("recipient")
      val messageIds = data.getLongArray("message_ids")

      Assert.assertEquals("Each job should contain two messages.", 2, messageIds.size.toLong())
      Assert.assertTrue("Each thread recipient pair should only exist once.", threadRecipientPairs.add(Pair(threadId, recipientId)))
    }

    Assert.assertEquals("Should have 9 total combinations.", 9, threadRecipientPairs.size.toLong())
  }

  private fun createMarkedMessageInfo(threadId: Long, recipientId: RecipientId): MarkedMessageInfo {
    return MarkedMessageInfo(
      threadId,
      SyncMessageId(recipientId, 0),
      MessageId(1),
      ExpirationInfo(0, 0, 0, false),
      StoryType.NONE
    )
  }
}
