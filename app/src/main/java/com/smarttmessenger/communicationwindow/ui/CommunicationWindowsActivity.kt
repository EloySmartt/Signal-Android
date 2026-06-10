package com.smarttmessenger.communicationwindow.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.R

/**
 * Hosts the communication-window navigation graph. Entry point is the bottom-sheet selector,
 * so this Activity is only ever opened to (a) create a new window or (b) view a window's details.
 * The start destination is set dynamically to avoid showing any intermediate list screen.
 */
class CommunicationWindowsActivity : PassphraseRequiredActivity() {

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    super.onCreate(savedInstanceState, ready)
    setContentView(R.layout.activity_smartt_communication_windows)

    if (savedInstanceState != null) return

    val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHost.navController
    val graph = navController.navInflater.inflate(R.navigation.smartt_setup_navigation)

    val windowId = intent.getStringExtra(EXTRA_WINDOW_ID)
    if (windowId != null) {
      graph.setStartDestination(R.id.windowDetailsFragment)
      navController.setGraph(graph, bundleOf("windowId" to windowId))
    } else {
      graph.setStartDestination(R.id.editWindowScheduleFragment)
      navController.setGraph(graph, null)
    }
  }

  companion object {
    private const val EXTRA_WINDOW_ID = "window_id"

    /** Opens the create-window wizard. */
    fun newIntentForCreate(context: Context): Intent =
      Intent(context, CommunicationWindowsActivity::class.java)

    /** Opens an existing window's detail screen. */
    fun newIntentForWindow(context: Context, windowId: String): Intent =
      Intent(context, CommunicationWindowsActivity::class.java)
        .putExtra(EXTRA_WINDOW_ID, windowId)
  }
}
