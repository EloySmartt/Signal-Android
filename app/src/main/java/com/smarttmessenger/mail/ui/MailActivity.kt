package com.smarttmessenger.mail.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.R

/**
 * Copied from CommunicationWindowsActivity — minimum changes:
 * - Inflates R.navigation.smartt_mail_navigation instead of smartt_setup_navigation
 * - Reuses the same generic NavHost layout (activity_smartt_communication_windows)
 * - Start destination: connect wizard, or landing when an account is already connected
 */
class MailActivity : PassphraseRequiredActivity() {

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    super.onCreate(savedInstanceState, ready)
    setContentView(R.layout.activity_smartt_communication_windows)

    if (savedInstanceState != null) return

    val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHost.navController
    val graph = navController.navInflater.inflate(R.navigation.smartt_mail_navigation)

    if (intent.getBooleanExtra(EXTRA_OPEN_LANDING, false)) {
      graph.setStartDestination(R.id.mailLandingFragment)
    } else {
      graph.setStartDestination(R.id.connectMailAccountFragment)
    }
    navController.setGraph(graph, null)
  }

  companion object {
    private const val EXTRA_OPEN_LANDING = "open_landing"

    /** Opens the connect-account wizard. */
    fun newIntentForConnect(context: Context): Intent =
      Intent(context, MailActivity::class.java)

    /** Opens the mail landing (inbox) screen. */
    fun newIntentForLanding(context: Context): Intent =
      Intent(context, MailActivity::class.java).putExtra(EXTRA_OPEN_LANDING, true)
  }
}
