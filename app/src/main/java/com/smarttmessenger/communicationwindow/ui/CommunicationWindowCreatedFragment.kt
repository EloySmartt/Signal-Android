package com.smarttmessenger.communicationwindow.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.thoughtcrime.securesms.LoggingFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.util.navigation.safeNavigate

// Copied from NotificationProfileCreatedFragment — minimum changes:
// - windowId: String args instead of profileId: Long
// - Navigate to windowDetailsFragment(windowId) instead of notificationProfileDetailsFragment(profileId)
// - No repository fetch needed — window tips are static, not schedule-dependent
// - Title: "Your communication window is ready"
// - Tips: cloud icon + grid icon (our feature-specific content)
class CommunicationWindowCreatedFragment : LoggingFragment(R.layout.fragment_notification_profile_created) {

  private val args: CommunicationWindowCreatedFragmentArgs by navArgs()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val topIcon: ImageView = view.findViewById(R.id.notification_profile_created_top_image)
    val topText: TextView = view.findViewById(R.id.notification_profile_created_top_text)
    val bottomIcon: ImageView = view.findViewById(R.id.notification_profile_created_bottom_image)
    val bottomText: TextView = view.findViewById(R.id.notification_profile_created_bottom_text)
    val title: TextView = view.findViewById(R.id.notification_profile_created_title)

    title.setText(R.string.CommunicationWindow__window_created)

    topIcon.setImageResource(R.drawable.smartt_messagestatus_held_24)
    topText.setText(R.string.CommunicationWindow__created_tip_cloud)

    bottomIcon.setImageResource(R.drawable.symbol_grid_square_24)
    bottomText.setText(R.string.CommunicationWindow__created_tip_shortcut)

    view.findViewById<View>(R.id.notification_profile_created_done).setOnClickListener {
      findNavController().safeNavigate(
        CommunicationWindowCreatedFragmentDirections.actionWindowCreatedFragmentToWindowDetailsFragment(args.windowId)
      )
    }
  }
}
