package com.smarttmessenger.app.jobs

import org.signal.core.util.logging.Log
import com.smarttmessenger.app.database.SignalDatabase
import com.smarttmessenger.app.jobmanager.Job
import com.smarttmessenger.app.keyvalue.SignalStore
import com.smarttmessenger.app.mms.OutgoingMessage
import com.smarttmessenger.app.net.NotPushRegisteredException
import com.smarttmessenger.app.recipients.Recipient
import com.smarttmessenger.app.sms.MessageSender

/**
 * Send payments activated message to all recipients of payment activation request
 */
class SendPaymentsActivatedJob(parameters: Parameters) : BaseJob(parameters) {

  companion object {
    private val TAG = Log.tag(SendPaymentsActivatedJob::class.java)

    const val KEY = "SendPaymentsActivatedJob"
  }

  constructor() : this(parameters = Parameters.Builder().build())

  override fun serialize(): ByteArray? = null

  override fun getFactoryKey(): String = KEY

  @Suppress("UsePropertyAccessSyntax")
  override fun onRun() {
    if (!Recipient.self().isRegistered) {
      throw NotPushRegisteredException()
    }

    if (!SignalStore.payments.mobileCoinPaymentsEnabled()) {
      Log.w(TAG, "Payments aren't enabled, not going to attempt to send activation messages.")
      return
    }

    val threadIds: List<Long> = SignalDatabase.messages.getIncomingPaymentRequestThreads()

    for (threadId in threadIds) {
      val recipient = SignalDatabase.threads.getRecipientForThreadId(threadId)
      if (recipient != null) {
        MessageSender.send(
          context,
          OutgoingMessage.paymentsActivatedMessage(recipient, System.currentTimeMillis(), 0),
          threadId,
          MessageSender.SendType.SIGNAL,
          null,
          null
        )
      } else {
        Log.w(TAG, "Unable to send activation message for thread: $threadId")
      }
    }
  }

  override fun onShouldRetry(e: Exception): Boolean {
    return false
  }

  override fun onFailure() {
    Log.w(TAG, "Failed to submit send of payments activated messages")
  }

  class Factory : Job.Factory<SendPaymentsActivatedJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): SendPaymentsActivatedJob {
      return SendPaymentsActivatedJob(parameters)
    }
  }
}
