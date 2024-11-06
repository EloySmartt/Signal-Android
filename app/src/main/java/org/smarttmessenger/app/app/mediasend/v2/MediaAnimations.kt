package com.smarttmessenger.app.mediasend.v2

import android.view.animation.Interpolator
import com.smarttmessenger.app.util.createDefaultCubicBezierInterpolator

object MediaAnimations {
  /**
   * Fast-In-Extra-Slow-Out Interpolator
   */
  @JvmStatic
  val interpolator: Interpolator = createDefaultCubicBezierInterpolator()
}
