package com.smarttmessenger.app.components.settings.app.subscription.receipts.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import com.smarttmessenger.app.database.model.InAppPaymentReceiptRecord
import com.smarttmessenger.app.util.livedata.Store

class DonationReceiptListPageViewModel(type: InAppPaymentReceiptRecord.Type?, repository: DonationReceiptListPageRepository) : ViewModel() {

  private val disposables = CompositeDisposable()
  private val store = Store(DonationReceiptListPageState())

  val state: LiveData<DonationReceiptListPageState> = store.stateLiveData

  init {
    disposables += repository.getRecords(type)
      .subscribe { records ->
        store.update {
          it.copy(
            records = records,
            isLoaded = true
          )
        }
      }
  }

  override fun onCleared() {
    disposables.clear()
  }

  class Factory(private val type: InAppPaymentReceiptRecord.Type?, private val repository: DonationReceiptListPageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return modelClass.cast(DonationReceiptListPageViewModel(type, repository)) as T
    }
  }
}
