/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.parental.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.smarttmessenger.parental.model.SmarttFilterCategory
import com.smarttmessenger.parental.model.SmarttFilterSensitivity
import com.smarttmessenger.parental.model.SmarttParentalCatalog
import com.smarttmessenger.parental.model.SmarttParentalToggle
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toPersistentSet

/**
 * What the Parental Control screen is showing. [locked] swaps the whole body for the locked notice.
 */
@Immutable
data class SmarttParentalState(
  val locked: Boolean = false,
  val toggles: ImmutableSet<SmarttParentalToggle> = SmarttParentalCatalog.DEFAULT_TOGGLES,
  val categories: ImmutableSet<SmarttFilterCategory> = SmarttParentalCatalog.DEFAULT_CATEGORIES,
  val sensitivity: SmarttFilterSensitivity = SmarttFilterSensitivity.BALANCED
) {
  fun isOn(toggle: SmarttParentalToggle): Boolean = toggle in toggles

  val windowsOn: Boolean
    get() = isOn(SmarttParentalToggle.WINDOWS)
}

/**
 * Drives the Parental Control screen.
 *
 * Everything here is a mockup: no filter runs, no window is enforced, no PIN is stored and nothing
 * is written to disk. State lives in the view model so it survives configuration changes, and dies
 * with the activity so a relaunch starts from the defaults again.
 */
class SmarttParentalViewModel : ViewModel() {

  private val internalState = mutableStateOf(SmarttParentalState())

  val state: State<SmarttParentalState> = internalState

  fun setToggle(toggle: SmarttParentalToggle, enabled: Boolean) {
    val current = internalState.value
    val updated = if (enabled) current.toggles + toggle else current.toggles - toggle
    internalState.value = current.copy(toggles = updated.toPersistentSet())
  }

  fun setCategory(category: SmarttFilterCategory, enabled: Boolean) {
    val current = internalState.value
    val updated = if (enabled) current.categories + category else current.categories - category
    internalState.value = current.copy(categories = updated.toPersistentSet())
  }

  fun setSensitivity(sensitivity: SmarttFilterSensitivity) {
    internalState.value = internalState.value.copy(sensitivity = sensitivity)
  }

  fun lock() {
    internalState.value = internalState.value.copy(locked = true)
  }

  fun unlock() {
    internalState.value = internalState.value.copy(locked = false)
  }
}
