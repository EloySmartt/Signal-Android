package com.smarttmessenger.communicationwindow.network

/** ThreadLocal carrier for the communication-window-held result of the last send call. */
object SmarttWindowHeldState {

  data class HeldInfo(val held: Boolean, val windowOpensAt: Long)

  private val threadLocal = ThreadLocal<HeldInfo>()

  fun set(info: HeldInfo) = threadLocal.set(info)
  fun get(): HeldInfo? = threadLocal.get()
  fun clear() = threadLocal.remove()
}
