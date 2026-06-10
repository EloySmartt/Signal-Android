package com.smarttmessenger.communicationwindow.network

import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

/**
 * OkHttp interceptor that reads the `communicationWindowHeld` and `windowOpensAt`
 * fields from PUT /v1/messages/{destination} responses and stores them in [SmarttWindowHeldState].
 *
 * Works because Signal uses synchronous OkHttp calls on job threads, so the ThreadLocal
 * set here is readable by IndividualSendJob.onRun() on the same thread.
 */
class SmarttWindowHeldInterceptor : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val response = chain.proceed(request)

    if (request.method != "PUT" || !request.url.encodedPath.startsWith("/v1/messages/")) {
      return response
    }

    SmarttWindowHeldState.clear()

    if (response.code == 200) {
      try {
        val peeked = response.peekBody(512).string()
        val json = JSONObject(peeked)
        if (json.optBoolean("communicationWindowHeld", false)) {
          SmarttWindowHeldState.set(
            SmarttWindowHeldState.HeldInfo(
              held = true,
              windowOpensAt = json.optLong("windowOpensAt", 0L)
            )
          )
        }
      } catch (e: Exception) { }
    }

    return response
  }
}
