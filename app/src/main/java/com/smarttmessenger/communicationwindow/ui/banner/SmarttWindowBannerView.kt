package com.smarttmessenger.communicationwindow.ui.banner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.smarttmessenger.communicationwindow.network.WindowMetadataResponse
import org.thoughtcrime.securesms.R

class SmarttWindowBannerView(context: Context) {

  val root: View = LayoutInflater.from(context)
    .inflate(R.layout.smartt_window_banner, null, false)

  private val title: TextView = root.findViewById(R.id.smartt_banner_title)
  private val toggle: ImageView = root.findViewById(R.id.smartt_banner_toggle)
  private val details: LinearLayout = root.findViewById(R.id.smartt_banner_details)
  private val checkFrequency: TextView = root.findViewById(R.id.smartt_banner_check_frequency)
  private val replyTime: TextView = root.findViewById(R.id.smartt_banner_reply_time)
  private val personalNote: TextView = root.findViewById(R.id.smartt_banner_personal_note)

  private var expanded = true

  init {
    toggle.setOnClickListener { toggleExpanded() }
    root.isVisible = false
  }

  fun bind(recipientName: String, metadata: WindowMetadataResponse) {
    if (!metadata.windowActive) {
      root.isVisible = false
      return
    }

    val startFormatted = minutesToAmPm(metadata.windowStartMinutes)
    val endFormatted = minutesToAmPm(metadata.windowEndMinutes)
    title.text = "${recipientName}'s communication window is closed from $startFormatted to $endFormatted"

    val exp = metadata.expectations
    if (exp != null) {
      exp.checkFrequency?.let {
        checkFrequency.text = "Usual check frequency: ${it.lowercase().replace('_', ' ')}"
        checkFrequency.isVisible = true
      }
      exp.usualReplyTime?.let {
        replyTime.text = "Usual reply time: ${it.lowercase().replace('_', ' ')}"
        replyTime.isVisible = true
      }
      exp.personalNote?.let {
        personalNote.text = "\"$it\""
        personalNote.isVisible = true
      }
    }

    root.isVisible = true
    setExpanded(true)
  }

  fun hide() {
    root.isVisible = false
  }

  private fun toggleExpanded() = setExpanded(!expanded)

  private fun setExpanded(expand: Boolean) {
    expanded = expand
    details.isVisible = expand
    toggle.rotation = if (expand) 0f else 180f
  }

  private fun minutesToAmPm(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    val amPm = if (h < 12) "AM" else "PM"
    val displayH = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
    return "%d:%02d %s".format(displayH, m, amPm)
  }
}
