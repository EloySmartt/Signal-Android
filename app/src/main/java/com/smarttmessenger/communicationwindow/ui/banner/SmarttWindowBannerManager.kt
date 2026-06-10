package com.smarttmessenger.communicationwindow.ui.banner

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import com.smarttmessenger.communicationwindow.repository.CommunicationWindowsRepository
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.thoughtcrime.securesms.recipients.Recipient

/**
 * Manages the communication window banner inside the conversation.
 * Injected programmatically into conversation_banner_frame — no XML changes to Signal's layouts.
 */
class SmarttWindowBannerManager(
  private val context: Context,
  private val container: ViewGroup,
  private val repository: CommunicationWindowsRepository
) {
  private val bannerView = SmarttWindowBannerView(context)
  private var metadataDisposable: Disposable? = null

  init {
    container.addView(bannerView.root)
  }

  fun onRecipientChanged(recipient: Recipient) {
    val aci = runCatching { recipient.requireAci().toString() }.getOrNull()
    if (aci == null || recipient.isGroup || recipient.isSelf) {
      bannerView.hide()
      return
    }

    metadataDisposable?.dispose()
    metadataDisposable = repository.getWindowMetadata(aci)
      .subscribeBy(
        onSuccess = { metadata ->
          Handler(Looper.getMainLooper()).post {
            if (metadata.windowActive) {
              bannerView.bind(recipient.getDisplayName(context), metadata)
            } else {
              bannerView.hide()
            }
          }
        },
        onError = { bannerView.hide() }
      )
  }

  fun clear() {
    metadataDisposable?.dispose()
    bannerView.hide()
  }
}
