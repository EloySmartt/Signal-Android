package com.smarttmessenger.app.video.interfaces

fun interface TranscoderCancelationSignal {
  fun isCanceled(): Boolean
}
