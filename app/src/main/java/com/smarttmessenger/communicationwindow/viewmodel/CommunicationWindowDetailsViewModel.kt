package com.smarttmessenger.communicationwindow.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smarttmessenger.communicationwindow.model.CommunicationWindow
import com.smarttmessenger.communicationwindow.model.WindowExpectations
import com.smarttmessenger.communicationwindow.repository.CommunicationWindowsRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy

/**
 * Mirrors NotificationProfileDetailsViewModel: reads reactively from the local table (auto-updates
 * on any change), and every edit is local-first via [CommunicationWindowsRepository.save] — the
 * server push happens in the background, so toggles/chips/removals are instant.
 */
class CommunicationWindowDetailsViewModel(
  private val windowId: String,
  private val repository: CommunicationWindowsRepository
) : ViewModel() {

  private val disposables = CompositeDisposable()
  private val _state = MutableLiveData<State>(State.NotLoaded)
  val state: LiveData<State> = _state

  init {
    // Driven by the windows list (like NP's getProfiles): when this window is deleted, the list
    // re-emits without it -> State.Invalid via onNext -> the screen leaves. No onError race.
    disposables += repository.getWindows()
      .map { windows -> windows.firstOrNull { it.windowId == windowId }?.let { State.Valid(it) } ?: State.Invalid }
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(onNext = { _state.value = it })
  }

  override fun onCleared() {
    super.onCleared()
    disposables.dispose()
  }

  private fun current(): CommunicationWindow? = (_state.value as? State.Valid)?.window

  private fun save(transform: (CommunicationWindow) -> CommunicationWindow): Completable {
    val window = current() ?: return Completable.complete()
    return repository.save(transform(window)).ignoreElement().observeOn(AndroidSchedulers.mainThread())
  }

  fun toggleEnabled(): Completable = save { it.copy(enabled = !it.enabled) }

  fun toggleAllowCallsFromExceptions(): Completable = save { it.copy(allowCallsFromExceptions = !it.allowCallsFromExceptions) }

  fun toggleAllowCallsFromAll(): Completable = save { it.copy(allowCallsFromAll = !it.allowCallsFromAll) }

  fun setExpectations(expectations: WindowExpectations): Completable = save { it.copy(expectations = expectations) }

  fun removeExceptionContact(serializedId: String): Completable = save { it.copy(exceptionContacts = it.exceptionContacts - serializedId) }

  fun addExceptionContact(serializedId: String): Completable = save { it.copy(exceptionContacts = it.exceptionContacts + serializedId) }

  fun deleteWindow(): Completable =
    repository.delete(windowId).observeOn(AndroidSchedulers.mainThread())

  sealed class State {
    object NotLoaded : State()
    object Invalid : State()
    data class Valid(val window: CommunicationWindow) : State()
  }

  class Factory(private val windowId: String, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      modelClass.cast(CommunicationWindowDetailsViewModel(windowId, CommunicationWindowsRepository(context)))!!
  }
}
