package com.smarttmessenger.mail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smarttmessenger.mail.model.MailAccount
import com.smarttmessenger.mail.model.MailMessage
import com.smarttmessenger.mail.repository.MailRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy

/**
 * Drives the Mail tab. Mirrors CommunicationWindowDetailsViewModel: reactive local reads via the
 * repository, RxJava3, LiveData state. The delivered/waiting split reflects the held/deliver window.
 */
class MailLandingViewModel(private val repository: MailRepository) : ViewModel() {

  private val disposables = CompositeDisposable()
  private val _state = MutableLiveData(State())
  val state: LiveData<State> = _state

  data class State(
    val account: MailAccount? = null,
    val delivered: List<MailMessage> = emptyList(),
    val waiting: Int = 0
  )

  init {
    disposables += repository.getAccounts()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(
        onNext = { list -> _state.value = _state.value?.copy(account = list.firstOrNull()) },
        onError = {}
      )
    disposables += repository.getMessages()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(
        onNext = { msgs ->
          _state.value = _state.value?.copy(
            delivered = msgs.filter { !it.held }.sortedByDescending { it.receivedAtMs },
            waiting = msgs.count { it.held }
          )
        },
        onError = {}
      )
  }

  fun deliverNow() {
    disposables += repository.deliverHeld().subscribeBy(onError = {})
  }

  override fun onCleared() {
    disposables.clear()
  }

  class Factory(private val repository: MailRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return MailLandingViewModel(repository) as T
    }
  }
}
