package com.platform.app.presentation.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.model.Subcategory
import com.platform.app.domain.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagementViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagementUiState())
    val uiState: StateFlow<ManagementUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<ManagementUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<ManagementUiEffect> = _effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialCategoriesIfEmpty()
            repository.seedInitialFinancialAccountsIfEmpty()
            repository.seedInitialPaymentMethodsIfEmpty()
        }
        loadData()
    }

    fun onAction(action: ManagementUiAction) {
        when (action) {
            is ManagementUiAction.SelectTab -> _uiState.update { it.copy(selectedTab = action.index) }
            is ManagementUiAction.SaveAccount -> handleSaveAccount(action.account)
            is ManagementUiAction.DeleteAccount -> handleDeleteAccount(action.accountId)
            is ManagementUiAction.SavePaymentMethod -> handleSavePaymentMethod(action.method)
            is ManagementUiAction.DeletePaymentMethod -> handleDeletePaymentMethod(action.methodId)
            is ManagementUiAction.SaveCategory -> handleSaveCategory(action.category)
            is ManagementUiAction.DeleteCategory -> handleDeleteCategory(action.categoryId)
            is ManagementUiAction.SaveSubcategory -> handleSaveSubcategory(action.subcategory)
            is ManagementUiAction.DeleteSubcategory -> handleDeleteSubcategory(action.subcategoryId)
            is ManagementUiAction.Refresh -> loadData()
        }
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        combine(
            repository.getFinancialAccounts(),
            repository.getPaymentMethods(),
            repository.getCategories(),
            repository.getAllSubcategories(),
            repository.getAllInstallments()
        ) { accounts, methods, categories, subcategories, installments ->
            _uiState.update {
                it.copy(
                    accounts = accounts,
                    paymentMethods = methods,
                    categories = categories,
                    subcategories = subcategories,
                    installments = installments,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }.catch { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Erro ao carregar cadastros."
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun handleSaveAccount(account: FinancialAccount) {
        viewModelScope.launch {
            try {
                repository.saveFinancialAccount(account)
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Conta '${account.name}' salva com sucesso!"))
                _effectChannel.send(ManagementUiEffect.ItemSaved)
            } catch (e: Exception) {
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Erro ao salvar conta: ${e.message}"))
            }
        }
    }

    private fun handleDeleteAccount(accountId: String) {
        viewModelScope.launch {
            try {
                repository.deleteFinancialAccount(accountId)
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Conta excluída com sucesso."))
            } catch (e: Exception) {
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Erro ao excluir conta: ${e.message}"))
            }
        }
    }

    private fun handleSavePaymentMethod(method: PaymentMethod) {
        viewModelScope.launch {
            try {
                repository.savePaymentMethod(method)
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Forma de pagamento '${method.name}' salva!"))
                _effectChannel.send(ManagementUiEffect.ItemSaved)
            } catch (e: Exception) {
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Erro ao salvar forma de pagamento: ${e.message}"))
            }
        }
    }

    private fun handleDeletePaymentMethod(methodId: String) {
        viewModelScope.launch {
            try {
                repository.deletePaymentMethod(methodId)
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Forma de pagamento excluída."))
            } catch (e: Exception) {
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Erro ao excluir forma de pagamento: ${e.message}"))
            }
        }
    }

    private fun handleSaveCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.saveCategory(category)
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Categoria '${category.name}' salva!"))
                _effectChannel.send(ManagementUiEffect.ItemSaved)
            } catch (e: Exception) {
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Erro ao salvar categoria: ${e.message}"))
            }
        }
    }

    private fun handleDeleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(categoryId)
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Categoria excluída."))
            } catch (e: Exception) {
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Erro ao excluir categoria: ${e.message}"))
            }
        }
    }

    private fun handleSaveSubcategory(subcategory: Subcategory) {
        viewModelScope.launch {
            try {
                repository.saveSubcategory(subcategory)
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Subcategoria '${subcategory.name}' salva!"))
                _effectChannel.send(ManagementUiEffect.ItemSaved)
            } catch (e: Exception) {
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Erro ao salvar subcategoria: ${e.message}"))
            }
        }
    }

    private fun handleDeleteSubcategory(subcategoryId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSubcategory(subcategoryId)
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Subcategoria excluída."))
            } catch (e: Exception) {
                _effectChannel.send(ManagementUiEffect.ShowSnackbar("Erro ao excluir subcategoria: ${e.message}"))
            }
        }
    }
}
