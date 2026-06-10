package com.smarttmessenger.communicationwindow.ui

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.smarttmessenger.communicationwindow.model.WindowExpectations
import com.smarttmessenger.communicationwindow.viewmodel.CommunicationWindowDraftViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.util.concurrent.LifecycleDisposable
import org.thoughtcrime.securesms.LoggingFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.util.views.CircularProgressMaterialButton

/**
 * Step 3 of the create wizard (also reused for single-screen edits from detail).
 * Check frequency + reply time are single-select choice chips (Material ChipGroup), and a free-text
 * personal note. All written to the shared draft.
 */
class SetWindowExpectationsFragment : LoggingFragment(R.layout.fragment_smartt_set_expectations) {

  private val draftVm: CommunicationWindowDraftViewModel by activityViewModels(
    factoryProducer = { CommunicationWindowDraftViewModel.Factory(requireContext().applicationContext) }
  )
  private val lifecycleDisposable = LifecycleDisposable()

  private val frequencyChips = mutableMapOf<Int, WindowExpectations.CheckFrequency>()
  private val replyChips = mutableMapOf<Int, WindowExpectations.UsualReplyTime>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    lifecycleDisposable.bindTo(viewLifecycleOwner.lifecycle)

    val editing = draftVm.isEditing
    val expectations = draftVm.current().expectations

    val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

    val frequencyGroup: ChipGroup = view.findViewById(R.id.check_frequency_group)
    WindowExpectations.CheckFrequency.values().forEach { freq ->
      val chip = layoutInflater.inflate(R.layout.smartt_expectation_chip, frequencyGroup, false) as Chip
      chip.id = View.generateViewId()
      chip.text = freq.label
      chip.isChecked = expectations.checkFrequency == freq
      frequencyChips[chip.id] = freq
      frequencyGroup.addView(chip)
    }

    val replyGroup: ChipGroup = view.findViewById(R.id.usual_reply_time_group)
    WindowExpectations.UsualReplyTime.values().forEach { time ->
      val chip = layoutInflater.inflate(R.layout.smartt_expectation_chip, replyGroup, false) as Chip
      chip.id = View.generateViewId()
      chip.text = time.label
      chip.isChecked = expectations.usualReplyTime == time
      replyChips[chip.id] = time
      replyGroup.addView(chip)
    }

    val noteInput: EditText = view.findViewById(R.id.personal_note_input)
    noteInput.setText(expectations.personalNote)

    val next: CircularProgressMaterialButton = view.findViewById(R.id.next_button)
    next.setText(if (editing) R.string.EditNotificationProfileSchedule__save else R.string.CommunicationWindow__next)
    next.setOnClickListener {
      draftVm.update {
        it.copy(
          expectations = WindowExpectations(
            checkFrequency = frequencyChips[frequencyGroup.checkedChipId],
            usualReplyTime = replyChips[replyGroup.checkedChipId],
            personalNote = noteInput.text?.toString()?.trim()?.takeIf { note -> note.isNotEmpty() }
          )
        )
      }

      if (editing) {
        lifecycleDisposable += draftVm.persist()
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSubscribe { next.setSpinning() }
          .doAfterTerminate { next.cancelSpinning() }
          .subscribeBy(onSuccess = { findNavController().navigateUp() })
      } else {
        findNavController().navigate(R.id.action_setExpectationsFragment_to_editWindowNameFragment)
      }
    }
  }
}
