package com.smarttmessenger.mail.ui

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.CheckedTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.smarttmessenger.communicationwindow.model.CommunicationWindowSchedule
import com.smarttmessenger.mail.viewmodel.MailDraftViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.util.concurrent.LifecycleDisposable
import org.thoughtcrime.securesms.LoggingFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.util.ViewUtil
import org.thoughtcrime.securesms.util.formatHours
import org.thoughtcrime.securesms.util.navigation.safeNavigate
import org.thoughtcrime.securesms.util.orderOfDaysInWeek
import org.thoughtcrime.securesms.util.views.CircularProgressMaterialButton
import org.thoughtcrime.securesms.util.visible
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DAY_TO_STARTING_LETTER: Map<DayOfWeek, Int> = mapOf(
  DayOfWeek.SUNDAY to R.string.EditNotificationProfileSchedule__sunday_first_letter,
  DayOfWeek.MONDAY to R.string.EditNotificationProfileSchedule__monday_first_letter,
  DayOfWeek.TUESDAY to R.string.EditNotificationProfileSchedule__tuesday_first_letter,
  DayOfWeek.WEDNESDAY to R.string.EditNotificationProfileSchedule__wednesday_first_letter,
  DayOfWeek.THURSDAY to R.string.EditNotificationProfileSchedule__thursday_first_letter,
  DayOfWeek.FRIDAY to R.string.EditNotificationProfileSchedule__friday_first_letter,
  DayOfWeek.SATURDAY to R.string.EditNotificationProfileSchedule__saturday_first_letter
)

/**
 * Copied from EditCommunicationWindowScheduleFragment — minimum changes:
 * - Draft is MailDraftViewModel (delivery window on the MailAccount) instead of the window draft
 * - Texts: "delivery window" wording (Mail__ strings)
 * - Next: persist + go to mailReadyFragment (no exception-contacts step for mail)
 * Reuses the exact same layout (fragment_edit_notification_profile_schedule), so it looks identical.
 */
class EditMailDeliveryScheduleFragment : LoggingFragment(R.layout.fragment_edit_notification_profile_schedule) {

  private val draftVm: MailDraftViewModel by activityViewModels(
    factoryProducer = { MailDraftViewModel.Factory() }
  )
  private val lifecycleDisposable = LifecycleDisposable()

  private fun schedule(): CommunicationWindowSchedule =
    draftVm.current().deliveryWindow.schedules.firstOrNull() ?: CommunicationWindowSchedule()

  private fun setSchedule(transform: (CommunicationWindowSchedule) -> CommunicationWindowSchedule) {
    draftVm.update { it.copy(deliveryWindow = it.deliveryWindow.copy(schedules = listOf(transform(schedule())))) }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    lifecycleDisposable.bindTo(viewLifecycleOwner.lifecycle)

    val editing = draftVm.isEditing

    val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    toolbar.setNavigationOnClickListener {
      if (!findNavController().navigateUp()) {
        requireActivity().finish()
      }
    }
    toolbar.title = if (editing) getString(R.string.Mail__edit_delivery_window) else null

    val title: TextView = view.findViewById(R.id.edit_notification_profile_schedule_title)
    val description: TextView = view.findViewById(R.id.edit_notification_profile_schedule_description)
    title.visible = !editing
    description.visible = !editing
    if (!editing) {
      title.setText(R.string.Mail__delivery_window)
      description.setText(R.string.Mail__delivery_window_description)
    }

    val enableToggle: MaterialSwitch = view.findViewById(R.id.edit_notification_profile_schedule_switch)
    enableToggle.setOnClickListener {
      draftVm.update { it.copy(deliveryWindow = it.deliveryWindow.copy(scheduled = enableToggle.isChecked)) }
    }

    val startTime: TextView = view.findViewById(R.id.edit_notification_profile_schedule_start_time)
    val endTime: TextView = view.findViewById(R.id.edit_notification_profile_schedule_end_time)

    val day1: CheckedTextView = view.findViewById(R.id.edit_notification_profile_schedule_day_1)
    val day2: CheckedTextView = view.findViewById(R.id.edit_notification_profile_schedule_day_2)
    val day3: CheckedTextView = view.findViewById(R.id.edit_notification_profile_schedule_day_3)
    val day4: CheckedTextView = view.findViewById(R.id.edit_notification_profile_schedule_day_4)
    val day5: CheckedTextView = view.findViewById(R.id.edit_notification_profile_schedule_day_5)
    val day6: CheckedTextView = view.findViewById(R.id.edit_notification_profile_schedule_day_6)
    val day7: CheckedTextView = view.findViewById(R.id.edit_notification_profile_schedule_day_7)

    val days: Map<CheckedTextView, DayOfWeek> = listOf(day1, day2, day3, day4, day5, day6, day7)
      .zip(Locale.getDefault().orderOfDaysInWeek()).toMap()

    days.forEach { (dayView, day) ->
      DrawableCompat.setTintList(dayView.background, ContextCompat.getColorStateList(dayView.context, R.color.notification_profile_schedule_background_tint))
      dayView.setText(DAY_TO_STARTING_LETTER[day]!!)
      dayView.setOnClickListener {
        setSchedule { s ->
          val newDays = s.daysEnabled.toMutableSet()
          if (newDays.contains(day.value)) newDays.remove(day.value) else newDays.add(day.value)
          s.copy(daysEnabled = newDays)
        }
      }
    }

    val next: CircularProgressMaterialButton = view.findViewById(R.id.edit_notification_profile_schedule__next)
    next.setText(if (editing) R.string.EditNotificationProfileSchedule__save else R.string.CommunicationWindow__next)
    next.setOnClickListener { onNext(next, editing) }

    draftVm.draft.observe(viewLifecycleOwner) {
      val schedule = schedule()
      val window = draftVm.current().deliveryWindow
      enableToggle.isEnabled = true
      enableToggle.isChecked = window.scheduled
      next.isEnabled = true

      days.forEach { (dayView, day) ->
        dayView.isChecked = schedule.daysEnabled.contains(day.value)
        dayView.isEnabled = window.scheduled
      }

      val start = LocalTime.of(schedule.start / 60, schedule.start % 60)
      startTime.text = start.formatTime(view.context)
      startTime.setOnClickListener { showTimeSelector(true, start) }
      startTime.isEnabled = window.scheduled

      val end = LocalTime.of(schedule.end / 60, schedule.end % 60)
      endTime.text = end.formatTime(view.context)
      endTime.setOnClickListener { showTimeSelector(false, end) }
      endTime.isEnabled = window.scheduled
    }
  }

  private fun onNext(button: CircularProgressMaterialButton, editing: Boolean) {
    val schedule = schedule()
    if (draftVm.current().deliveryWindow.scheduled && schedule.daysEnabled.isEmpty()) {
      Toast.makeText(requireContext(), R.string.EditNotificationProfileSchedule__schedule_must_have_at_least_one_day, Toast.LENGTH_LONG).show()
      return
    }

    lifecycleDisposable += draftVm.persist()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSubscribe { button.setSpinning() }
      .doAfterTerminate { button.cancelSpinning() }
      .subscribeBy(onSuccess = {
        if (editing) {
          findNavController().navigateUp()
        } else {
          findNavController().safeNavigate(R.id.action_editMailDeliveryScheduleFragment_to_mailReadyFragment)
        }
      })
  }

  private fun showTimeSelector(isStart: Boolean, time: LocalTime) {
    val timeFormat = if (DateFormat.is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
    val picker = MaterialTimePicker.Builder()
      .setTimeFormat(timeFormat)
      .setHour(time.hour)
      .setMinute(time.minute)
      .setTitleText(if (isStart) R.string.EditNotificationProfileSchedule__set_start_time else R.string.EditNotificationProfileSchedule__set_end_time)
      .build()

    picker.addOnDismissListener {
      picker.clearOnDismissListeners()
      picker.clearOnPositiveButtonClickListeners()
    }
    picker.addOnPositiveButtonClickListener {
      val minutes = picker.hour * 60 + picker.minute
      if (isStart) setSchedule { it.copy(start = minutes) } else setSchedule { it.copy(end = minutes) }
    }
    picker.show(childFragmentManager, "MAIL_TIME_PICKER")
  }
}

private fun LocalTime.formatTime(context: Context): SpannableString {
  val amPm = DateTimeFormatter.ofPattern("a").format(this)
  val formattedTime = this.formatHours(context)
  return SpannableString(formattedTime).apply {
    val idx = formattedTime.indexOf(amPm, ignoreCase = true)
    if (idx != -1) setSpan(AbsoluteSizeSpan(ViewUtil.spToPx(20f)), idx, idx + amPm.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
}
