package com.smarttmessenger.communicationwindow.ui.models

import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.smarttmessenger.communicationwindow.model.WindowExpectations
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.PreferenceModel
import org.thoughtcrime.securesms.util.adapter.mapping.LayoutFactory
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.adapter.mapping.MappingViewHolder

/**
 * Inline expectations editor for the window detail screen: check-frequency + reply-time choice chips
 * and a free-text personal note. Reuses [R.layout.smartt_expectation_chip] (the same chip the create
 * wizard uses). Changes are reported through [Model.onChanged] — chips on selection, the note on
 * focus loss — so the detail can persist immediately.
 */
object WindowExpectationsPreference {

  fun register(adapter: MappingAdapter) {
    adapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.smartt_window_expectations_pref))
  }

  class Model(
    val expectations: WindowExpectations,
    val onChanged: (WindowExpectations) -> Unit
  ) : PreferenceModel<Model>() {
    // There is only ever one expectations row, so identity is constant.
    override fun areItemsTheSame(newItem: Model): Boolean = true

    override fun areContentsTheSame(newItem: Model): Boolean {
      return super.areContentsTheSame(newItem) && expectations == newItem.expectations
    }
  }

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val frequencyGroup: ChipGroup = findViewById(R.id.check_frequency_group)
    private val replyGroup: ChipGroup = findViewById(R.id.usual_reply_time_group)
    private val noteInput: EditText = findViewById(R.id.personal_note_input)

    private val frequencyChips = mutableMapOf<Int, WindowExpectations.CheckFrequency>()
    private val replyChips = mutableMapOf<Int, WindowExpectations.UsualReplyTime>()

    /** Guards the listeners while we set chip/note state programmatically. */
    private var binding = false
    private var current: Model? = null

    override fun bind(model: Model) {
      current = model

      if (frequencyGroup.childCount == 0) {
        val inflater = LayoutInflater.from(context)
        WindowExpectations.CheckFrequency.values().forEach { freq ->
          val chip = inflater.inflate(R.layout.smartt_expectation_chip, frequencyGroup, false) as Chip
          chip.id = View.generateViewId()
          chip.text = freq.label
          chip.setOnCheckedChangeListener { _, isChecked -> if (isChecked && !binding) emit() }
          frequencyChips[chip.id] = freq
          frequencyGroup.addView(chip)
        }
        WindowExpectations.UsualReplyTime.values().forEach { time ->
          val chip = inflater.inflate(R.layout.smartt_expectation_chip, replyGroup, false) as Chip
          chip.id = View.generateViewId()
          chip.text = time.label
          chip.setOnCheckedChangeListener { _, isChecked -> if (isChecked && !binding) emit() }
          replyChips[chip.id] = time
          replyGroup.addView(chip)
        }
        noteInput.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && !binding) emit() }
      }

      binding = true
      frequencyChips.forEach { (id, freq) -> frequencyGroup.findViewById<Chip>(id).isChecked = model.expectations.checkFrequency == freq }
      replyChips.forEach { (id, time) -> replyGroup.findViewById<Chip>(id).isChecked = model.expectations.usualReplyTime == time }
      if (!noteInput.hasFocus()) {
        noteInput.setText(model.expectations.personalNote ?: "")
      }
      binding = false
    }

    private fun emit() {
      val model = current ?: return
      val expectations = WindowExpectations(
        checkFrequency = frequencyChips[frequencyGroup.checkedChipId],
        usualReplyTime = replyChips[replyGroup.checkedChipId],
        personalNote = noteInput.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
      )
      if (expectations != model.expectations) {
        model.onChanged(expectations)
      }
    }
  }
}
