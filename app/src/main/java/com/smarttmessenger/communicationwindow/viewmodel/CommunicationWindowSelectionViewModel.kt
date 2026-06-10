package com.smarttmessenger.communicationwindow.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smarttmessenger.communicationwindow.model.CommunicationWindow
import com.smarttmessenger.communicationwindow.repository.CommunicationWindowsRepository
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.thoughtcrime.securesms.util.livedata.Store

/**
 * Backs the communication-window bottom sheet selector.
 * Mirrors NotificationProfileSelectionViewModel.
 */
class CommunicationWindowSelectionViewModel(repository: CommunicationWindowsRepository) : ViewModel() {

  private val store = Store(emptyList<CommunicationWindow>())
  val state: LiveData<List<CommunicationWindow>> = store.stateLiveData

  private val disposables = CompositeDisposable()

  init {
    disposables += repository.getWindows()
      .subscribeBy(onNext = { windows -> store.update { windows } })
  }

  override fun onCleared() {
    disposables.clear()
  }

  class Factory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      modelClass.cast(CommunicationWindowSelectionViewModel(CommunicationWindowsRepository(context)))!!
  }
}
