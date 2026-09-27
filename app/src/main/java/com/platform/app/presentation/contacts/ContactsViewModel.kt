package com.platform.app.presentation.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.model.Contact
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.GetContactDetailsUseCase
import com.platform.app.domain.usecase.ToggleInstallmentPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val repository: FinancialRepository,
    private val getContactDetailsUseCase: GetContactDetailsUseCase,
    private val togglePaymentUseCase: ToggleInstallmentPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<ContactsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<ContactsUiEffect> = _effectChannel.receiveAsFlow()

    private var detailsJob: Job? = null

    init {
        loadContacts()
    }

    fun onAction(action: ContactsUiAction) {
        when (action) {
            is ContactsUiAction.SaveContact -> handleSaveContact(action.contact)
            is ContactsUiAction.DeleteContact -> handleDeleteContact(action.contactId)
            is ContactsUiAction.SearchQueryChanged -> handleSearchQuery(action.query)
            is ContactsUiAction.LoadContactDetails -> loadContactDetails(action.contactId)
            is ContactsUiAction.ToggleInstallmentPayment -> handleTogglePayment(action.installmentId, action.isPaid)
            is ContactsUiAction.Refresh -> loadContacts()
        }
    }

    private fun loadContacts() {
        _uiState.update { it.copy(isLoading = true) }
        repository.getContacts()
            .onEach { contacts ->
                _uiState.update { current ->
                    val filtered = applySearch(contacts, current.searchQuery)
                    current.copy(
                        contacts = contacts,
                        filteredContacts = filtered,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Erro ao carregar contatos."
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleSearchQuery(query: String) {
        _uiState.update { current ->
            val filtered = applySearch(current.contacts, query)
            current.copy(searchQuery = query, filteredContacts = filtered)
        }
    }

    private fun applySearch(contacts: List<Contact>, query: String): List<Contact> {
        if (query.isBlank()) return contacts
        return contacts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.phone.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true) ||
            it.city.contains(query, ignoreCase = true)
        }
    }

    private fun handleSaveContact(contact: Contact) {
        viewModelScope.launch {
            try {
                repository.saveContact(contact)
                _effectChannel.send(ContactsUiEffect.ShowSnackbar("Contato '${contact.name}' salvo com sucesso!"))
                _effectChannel.send(ContactsUiEffect.ContactSaved)
            } catch (e: Exception) {
                _effectChannel.send(ContactsUiEffect.ShowSnackbar("Erro ao salvar contato: ${e.message}"))
            }
        }
    }

    private fun handleDeleteContact(contactId: String) {
        viewModelScope.launch {
            try {
                repository.deleteContact(contactId)
                _effectChannel.send(ContactsUiEffect.ShowSnackbar("Contato excluído com sucesso."))
            } catch (e: Exception) {
                _effectChannel.send(ContactsUiEffect.ShowSnackbar("Erro ao excluir contato: ${e.message}"))
            }
        }
    }

    fun loadContactDetails(contactId: String) {
        detailsJob?.cancel()
        _uiState.update { it.copy(isDetailsLoading = true) }
        detailsJob = getContactDetailsUseCase(contactId)
            .onEach { details ->
                _uiState.update {
                    it.copy(
                        selectedContactDetails = details,
                        isDetailsLoading = false
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isDetailsLoading = false,
                        errorMessage = error.localizedMessage ?: "Erro ao carregar detalhes do contato."
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleTogglePayment(installmentId: String, currentPaid: Boolean) {
        viewModelScope.launch {
            try {
                togglePaymentUseCase(installmentId, currentPaid)
                val msg = if (!currentPaid) "Parcela marcada como paga!" else "Pagamento desfeito."
                _effectChannel.send(ContactsUiEffect.ShowSnackbar(msg))
            } catch (e: Exception) {
                _effectChannel.send(ContactsUiEffect.ShowSnackbar("Erro ao atualizar pagamento: ${e.message}"))
            }
        }
    }
}
