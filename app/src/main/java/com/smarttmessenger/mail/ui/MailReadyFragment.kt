package com.smarttmessenger.mail.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import org.thoughtcrime.securesms.LoggingFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.util.navigation.safeNavigate

/**
 * Copied from CommunicationWindowCreatedFragment — minimum changes:
 * - No args (single mail account)
 * - Title/tips: mail wording
 * - Done -> mailLandingFragment
 * Reuses the same layout (fragment_notification_profile_created), so it looks identical.
 */
class MailReadyFragment : LoggingFragment(R.layout.fragment_notification_profile_created) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val topIcon: ImageView = view.findViewById(R.id.notification_profile_created_top_image)
    val topText: TextView = view.findViewById(R.id.notification_profile_created_top_text)
    val bottomIcon: ImageView = view.findViewById(R.id.notification_profile_created_bottom_image)
    val bottomText: TextView = view.findViewById(R.id.notification_profile_created_bottom_text)
    val title: TextView = view.findViewById(R.id.notification_profile_created_title)

    title.setText(R.string.Mail__mail_ready)

    topIcon.setImageResource(R.drawable.smartt_messagestatus_held_24)
    topText.setText(R.string.Mail__ready_tip_held)

    bottomIcon.setImageResource(R.drawable.symbol_grid_square_24)
    bottomText.setText(R.string.Mail__ready_tip_deliver)

    view.findViewById<View>(R.id.notification_profile_created_done).setOnClickListener {
      findNavController().safeNavigate(R.id.action_mailReadyFragment_to_mailLandingFragment)
    }
  }
}
