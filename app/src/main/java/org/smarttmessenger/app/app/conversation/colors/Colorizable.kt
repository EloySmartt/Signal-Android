package com.smarttmessenger.app.conversation.colors

import android.view.ViewGroup
import com.smarttmessenger.app.util.ProjectionList

/**
 * Denotes that a class can be colorized. The class is responsible for
 * generating its own projection.
 */
interface Colorizable {
  fun getColorizerProjections(coordinateRoot: ViewGroup): ProjectionList
}
