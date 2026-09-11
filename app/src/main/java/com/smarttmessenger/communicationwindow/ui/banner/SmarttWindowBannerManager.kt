package com.smarttmessenger.communicationwindow.ui.banner

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import com.smarttmessenger.communicationwindow.repository.CommunicationWindowsRepository
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.dependencies.AppDependencies
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
  private val mainHandler = Handler(Looper.getMainLooper())
  private var metadataDisposable: Disposable? = null

  init {
    container.addView(bannerView.root)
  }

  fun onRecipientChanged(recipient: Recipient) {
    if (recipient.isGroup || recipient.isSelf) {
      bannerView.hide()
      return
    }

    // ACI when we know it, PNI otherwise — `toString()` emits the wire form the server parses
    // (bare UUID for an ACI, "PNI:<uuid>" for a PNI). Requiring an ACI here used to skip the
    // request entirely: on a fresh install contacts are often PNI-only until a profile fetch or
    // the first exchanged message, so no banner ever appeared and nothing was logged.
    val serviceId = recipient.serviceId.orElse(null)?.toString()
    if (serviceId == null) {
      Log.d(TAG, "No service id for recipient ${recipient.id}; cannot ask for window metadata yet")
      bannerView.hide()
      return
    }

    metadataDisposable?.dispose()
    metadataDisposable = repository.getWindowMetadata(serviceId)
      .subscribeBy(
        onSuccess = { metadata ->
          mainHandler.post {
            if (metadata.windowActive) {
              bannerView.bind(recipient.getDisplayName(context), metadata)
            } else {
              bannerView.hide()
            }
          }
        },
        onError = { error ->
          // Rx delivers this on the IO thread; hiding touches views, so it has to be posted.
          Log.w(TAG, "Could not load window metadata; hiding banner", error)
          mainHandler.post { bannerView.hide() }
        }
      )
  }

  fun clear() {
    metadataDisposable?.dispose()
    bannerView.hide()
  }

  companion object {
    private val TAG = Log.tag(SmarttWindowBannerManager::class.java)

    /**
     * Creates the manager on first call (reusing [existing] afterwards) and refreshes the banner
     * for [recipient]. Keeps the lazy-construction logic here so the ConversationFragment hook
     * stays a single line.
     */
    @JvmStatic
    fun update(existing: SmarttWindowBannerManager?, context: Context, container: ViewGroup, recipient: Recipient): SmarttWindowBannerManager {
      val manager = existing ?: SmarttWindowBannerManager(context, container, AppDependencies.smarttCommunicationWindowRepository)
      manager.onRecipientChanged(recipient)
      return manager
    }
  }
}
