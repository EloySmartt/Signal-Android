package com.smarttmessenger.communicationwindow.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smarttmessenger.communicationwindow.model.CommunicationWindow
import com.smarttmessenger.communicationwindow.model.CommunicationWindowSchedule
import com.smarttmessenger.communicationwindow.repository.CommunicationWindowsRepository
import io.reactivex.rxjava3.core.Single
import org.thoughtcrime.securesms.recipients.RecipientId

/**
 * Activity-scoped draft shared across the create wizard
 * (schedule → allowed messages → set expectations → name).
 *
 * Exception contacts are held as [RecipientId]s for the UI (resolved with a plain
 * Recipient.resolved on the main thread, exactly like notification profiles). The ACI strings the
 * server speaks are produced only at [persist] and parsed only at [startEdit].
 */
class CommunicationWindowDraftViewModel(private val repository: CommunicationWindowsRepository) : ViewModel() {

  private val _draft = MutableLiveData(emptyDraft())
  val draft: LiveData<CommunicationWindow> = _draft

  private val _exceptionRecipients = MutableLiveData<List<RecipientId>>(emptyList())
  val exceptionRecipients: LiveData<List<RecipientId>> = _exceptionRecipients

  var editingWindowId: String? = null
    private set

  val isEditing: Boolean get() = editingWindowId != null

  fun current(): CommunicationWindow = _draft.value!!

  fun update(transform: (CommunicationWindow) -> CommunicationWindow) {
    _draft.value = transform(current())
  }

  fun setExceptionRecipients(ids: List<RecipientId>) {
    _exceptionRecipients.value = ids.distinct()
  }

  fun removeExceptionRecipient(id: RecipientId) {
    _exceptionRecipients.value = (_exceptionRecipients.value ?: emptyList()) - id
  }

  /** Begin a fresh create flow. */
  fun startCreate() {
    editingWindowId = null
    _draft.value = emptyDraft()
    _exceptionRecipients.value = emptyList()
  }

  /** Begin editing an existing window. Exception contacts are stored as RecipientIds (like NP members). */
  fun startEdit(window: CommunicationWindow) {
    editingWindowId = window.windowId
    _draft.value = window
    _exceptionRecipients.value = window.exceptionContacts.map { RecipientId.from(it) }
  }

  /** Create (new) or update (existing). Persists exception contacts as RecipientIds (like NP). */
  fun persist(): Single<CommunicationWindow> {
    val window = current().copy(exceptionContacts = (_exceptionRecipients.value ?: emptyList()).map { it.serialize() }.toSet())
    return repository.save(window)   // upsert (local-first); the client owns windowId
  }

  // Client-generated id (local-first, like NP's DB-assigned id) so create works offline too.
  private fun emptyDraft() = CommunicationWindow(
    windowId = java.util.UUID.randomUUID().toString(),
    enabled = true,
    schedules = listOf(CommunicationWindowSchedule())
  )

  class Factory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      modelClass.cast(CommunicationWindowDraftViewModel(CommunicationWindowsRepository(context)))!!
  }
}
