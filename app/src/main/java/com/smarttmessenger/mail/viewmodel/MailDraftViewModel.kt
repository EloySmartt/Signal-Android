package com.smarttmessenger.mail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smarttmessenger.mail.model.MailAccount
import com.smarttmessenger.mail.model.MailProvider
import com.smarttmessenger.mail.repository.MailRepository
import io.reactivex.rxjava3.core.Single

/**
 * Copied from CommunicationWindowDraftViewModel — minimum changes:
 * - Draft is a MailAccount (provider + delivery window) instead of a CommunicationWindow
 * - No exception recipients (mail has no contact exceptions)
 * Activity-scoped draft shared across the connect wizard (provider -> schedule -> ready).
 * Nothing is persisted until the final step.
 */
class MailDraftViewModel(private val repository: MailRepository) : ViewModel() {

  private val _draft = MutableLiveData(emptyDraft())
  val draft: LiveData<MailAccount> = _draft

  var editingAccountId: String? = null
    private set

  val isEditing: Boolean get() = editingAccountId != null

  fun current(): MailAccount = _draft.value!!

  fun update(transform: (MailAccount) -> MailAccount) {
    _draft.value = transform(current())
  }

  fun startCreate(provider: MailProvider) {
    editingAccountId = null
    _draft.value = emptyDraft().copy(provider = provider)
  }

  fun startEdit(account: MailAccount) {
    editingAccountId = account.accountId
    _draft.value = account
  }

  /** Connect (new) or update (existing). Local-first, like CommunicationWindowsRepository.save. */
  fun persist(): Single<MailAccount> {
    val account = current().copy(connectedAtMs = System.currentTimeMillis())
    return repository.connect(account)
  }

  private fun emptyDraft() = MailAccount(accountId = java.util.UUID.randomUUID().toString())

  class Factory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      modelClass.cast(MailDraftViewModel(SHARED_REPOSITORY))!!
  }

  companion object {
    /** Single in-memory repository for the scaffold; swap for a DB-backed singleton later. */
    val SHARED_REPOSITORY: MailRepository = MailRepository()
  }
}
