package com.smarttmessenger.communicationwindow.network

import android.util.Base64
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.smarttmessenger.communicationwindow.model.CommunicationWindow
import com.smarttmessenger.communicationwindow.model.CommunicationWindowSchedule
import com.smarttmessenger.communicationwindow.model.WindowExpectations
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.BuildConfig
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.recipients.RecipientId
import org.whispersystems.signalservice.api.push.ServiceId

/**
 * Authenticated HTTP client for the Smartt communication window API.
 * Uses Signal's OkHttpClient (correct TLS config) + Basic Auth from SignalStore.
 */
class SmarttCommunicationWindowApi(private val okHttpClient: OkHttpClient) {

  private val mapper = jacksonObjectMapper()
  private val json = "application/json; charset=utf-8".toMediaType()

  companion object {
    private val TAG = Log.tag(SmarttCommunicationWindowApi::class.java)
    private const val BASE = "/v1/smartt/communication-windows"
  }

  private fun baseUrl() = BuildConfig.SIGNAL_URL

  private fun authHeader(): String {
    val aci = runCatching { SignalStore.account.requireAci().toString() }.getOrDefault("")
    val password = SignalStore.account.servicePassword ?: ""
    val credential = "$aci:$password"
    return "Basic " + Base64.encodeToString(credential.toByteArray(), Base64.NO_WRAP)
  }

  private fun get(path: String): String {
    val request = Request.Builder()
      .url(baseUrl() + path)
      .header("Authorization", authHeader())
      .get()
      .build()
    okHttpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
      return response.body?.string() ?: ""
    }
  }

  private fun post(path: String, body: Any): String {
    val request = Request.Builder()
      .url(baseUrl() + path)
      .header("Authorization", authHeader())
      .post(mapper.writeValueAsString(body).toRequestBody(json))
      .build()
    okHttpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
      return response.body?.string() ?: ""
    }
  }

  private fun put(path: String, body: Any): String {
    val request = Request.Builder()
      .url(baseUrl() + path)
      .header("Authorization", authHeader())
      .put(mapper.writeValueAsString(body).toRequestBody(json))
      .build()
    okHttpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
      return response.body?.string() ?: ""
    }
  }

  private fun delete(path: String) {
    val request = Request.Builder()
      .url(baseUrl() + path)
      .header("Authorization", authHeader())
      .delete()
      .build()
    okHttpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
    }
  }

  fun getWindows(): List<CommunicationWindowResponse> =
    mapper.readValue(get(BASE))

  fun createWindow(request: CommunicationWindowRequest): CommunicationWindowResponse =
    mapper.readValue(post(BASE, request))

  fun updateWindow(windowId: String, request: CommunicationWindowRequest): CommunicationWindowResponse =
    mapper.readValue(put("$BASE/$windowId", request))

  fun deleteWindow(windowId: String) = delete("$BASE/$windowId")

  /** [recipientServiceId] is an ACI (a bare UUID) or a PNI (`PNI:` + UUID); the server parses both. */
  fun getWindowMetadata(recipientServiceId: String): WindowMetadataResponse =
    mapper.readValue(get("$BASE/metadata/$recipientServiceId"))
}

// --- DTOs ---

data class CommunicationWindowRequest(
  @JsonProperty val name: String,
  @JsonProperty val emoji: String?,
  @JsonProperty val enabled: Boolean,
  @JsonProperty val schedules: List<ScheduleDto>,
  @JsonProperty val exceptionContacts: Set<String>,
  @JsonProperty val allowCallsFromExceptions: Boolean,
  @JsonProperty val allowCallsFromAll: Boolean,
  @JsonProperty val expectations: ExpectationsDto?
) {
  companion object {
    fun from(window: CommunicationWindow) = CommunicationWindowRequest(
      name = window.name,
      emoji = window.emoji,
      enabled = window.enabled,
      schedules = window.schedules.map { ScheduleDto.from(it) },
      // Local stores RecipientIds (like NP members); the server matches senders by ACI, so convert
      // at this boundary. Contacts whose ACI isn't cached locally are skipped from the server payload
      // only — they remain stored locally and visible in the UI.
      exceptionContacts = window.exceptionContacts.mapNotNull { serializedId ->
        runCatching { Recipient.resolved(RecipientId.from(serializedId)).requireAci().toString() }.getOrNull()
      }.toSet(),
      allowCallsFromExceptions = window.allowCallsFromExceptions,
      allowCallsFromAll = window.allowCallsFromAll,
      expectations = window.expectations.let {
        if (it.checkFrequency == null && it.usualReplyTime == null && it.personalNote == null) null
        else ExpectationsDto(it.checkFrequency?.name, it.usualReplyTime?.name, it.personalNote)
      }
    )
  }
}

data class CommunicationWindowResponse(
  @JsonProperty val windowId: String = "",
  @JsonProperty val name: String = "",
  @JsonProperty val emoji: String? = null,
  @JsonProperty val enabled: Boolean = true,
  @JsonProperty val schedules: List<ScheduleDto> = emptyList(),
  @JsonProperty val exceptionContacts: Set<String> = emptySet(),
  @JsonProperty val allowCallsFromExceptions: Boolean = true,
  @JsonProperty val allowCallsFromAll: Boolean = true,
  @JsonProperty val expectations: ExpectationsDto? = null
) {
  fun toModel() = CommunicationWindow(
    windowId = windowId, name = name, emoji = emoji ?: "",
    enabled = enabled, schedules = schedules.map { it.toModel() },
    // Server speaks ACIs; convert back to local RecipientIds (like NP members).
    exceptionContacts = exceptionContacts.mapNotNull { aci ->
      runCatching { Recipient.externalPush(ServiceId.parseOrThrow(aci)).id.serialize() }.getOrNull()
    }.toSet(),
    allowCallsFromExceptions = allowCallsFromExceptions,
    allowCallsFromAll = allowCallsFromAll,
    expectations = expectations?.toModel() ?: WindowExpectations()
  )
}

data class ScheduleDto(
  @JsonProperty val enabled: Boolean = true,
  @JsonProperty val start: Int = 9 * 60,
  @JsonProperty val end: Int = 17 * 60,
  @JsonProperty val daysEnabled: Set<Int> = setOf(1, 2, 3, 4, 5)
) {
  fun toModel() = CommunicationWindowSchedule(enabled, start, end, daysEnabled)
  companion object {
    fun from(s: CommunicationWindowSchedule) = ScheduleDto(s.enabled, s.start, s.end, s.daysEnabled)
  }
}

data class ExpectationsDto(
  @JsonProperty val checkFrequency: String? = null,
  @JsonProperty val usualReplyTime: String? = null,
  @JsonProperty val personalNote: String? = null
) {
  fun toModel() = WindowExpectations(
    checkFrequency = checkFrequency?.let { runCatching { WindowExpectations.CheckFrequency.valueOf(it) }.getOrNull() },
    usualReplyTime = usualReplyTime?.let { runCatching { WindowExpectations.UsualReplyTime.valueOf(it) }.getOrNull() },
    personalNote = personalNote
  )
}

data class WindowMetadataResponse(
  @JsonProperty val windowActive: Boolean = false,
  @JsonProperty val windowStartMinutes: Int = 0,
  @JsonProperty val windowEndMinutes: Int = 0,
  @JsonProperty val windowName: String? = null,
  @JsonProperty val expectations: ExpectationsDto? = null
)
