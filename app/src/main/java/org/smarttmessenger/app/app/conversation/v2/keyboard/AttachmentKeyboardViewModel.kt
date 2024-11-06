/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.conversation.v2.keyboard

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import com.smarttmessenger.app.mediasend.Media
import com.smarttmessenger.app.mediasend.MediaRepository

class AttachmentKeyboardViewModel(
  private val mediaRepository: MediaRepository = MediaRepository()
) : ViewModel() {

  private val refreshRecentMedia = BehaviorSubject.createDefault(Unit)

  fun getRecentMedia(): Observable<MutableList<Media>> {
    return refreshRecentMedia
      .flatMapSingle {
        mediaRepository
          .recentMedia
      }
      .observeOn(AndroidSchedulers.mainThread())
  }

  fun refreshRecentMedia() {
    refreshRecentMedia.onNext(Unit)
  }
}
