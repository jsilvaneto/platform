package com.platform.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.connectivity.NetworkMonitor
import com.platform.app.domain.model.PlatformItem
import com.platform.app.domain.repository.ItemRepository
import com.platform.app.domain.usecase.GetItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getItemsUseCase: GetItemsUseCase,
    private val repository: ItemRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<HomeUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<HomeUiEffect> = _effectChannel.receiveAsFlow()

    init {
        observeItems()
        observeConnectivity()
    }

    fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.AddItem -> addItem(action.title, action.description)
            is HomeUiAction.ToggleItemCompletion -> toggleItem(action.item)
            is HomeUiAction.DeleteItem -> deleteItem(action.id)
            is HomeUiAction.SearchQueryChanged -> updateSearchQuery(action.query)
            is HomeUiAction.FilterCompletedChanged -> updateFilter(action.filterCompleted)
            is HomeUiAction.ClearCompleted -> clearCompletedItems()
            is HomeUiAction.Refresh -> observeItems()
        }
    }

    private fun observeItems() {
        viewModelScope.launch {
            getItemsUseCase()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.localizedMessage ?: "Erro ao carregar itens locais."
                        )
                    }
                }
                .collect { items ->
                    _uiState.update { current ->
                        val filtered = applyFilter(items, current.searchQuery, current.filterCompleted)
                        current.copy(
                            items = items,
                            filteredItems = filtered,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isDeviceOnline = online) }
            }
        }
    }

    private fun addItem(title: String, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newItem = PlatformItem(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                description = description.trim(),
                isCompleted = false
            )
            repository.saveItem(newItem)
            _effectChannel.send(HomeUiEffect.ShowSnackbar("Item '${newItem.title}' salvo localmente."))
        }
    }

    private fun toggleItem(item: PlatformItem) {
        viewModelScope.launch {
            repository.toggleItemCompletion(item.id)
        }
    }

    private fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.deleteItem(id)
            _effectChannel.send(HomeUiEffect.ShowSnackbar("Item removido."))
        }
    }

    private fun clearCompletedItems() {
        viewModelScope.launch {
            repository.clearCompletedItems()
            _effectChannel.send(HomeUiEffect.ShowSnackbar("Itens concluídos foram limpos."))
        }
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { current ->
            val filtered = applyFilter(current.items, query, current.filterCompleted)
            current.copy(searchQuery = query, filteredItems = filtered)
        }
    }

    private fun updateFilter(filterCompleted: Boolean?) {
        _uiState.update { current ->
            val filtered = applyFilter(current.items, current.searchQuery, filterCompleted)
            current.copy(filterCompleted = filterCompleted, filteredItems = filtered)
        }
    }

    private fun applyFilter(
        items: List<PlatformItem>,
        query: String,
        filterCompleted: Boolean?
    ): List<PlatformItem> {
        return items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            val matchesStatus = when (filterCompleted) {
                null -> true
                true -> item.isCompleted
                false -> !item.isCompleted
            }
            matchesQuery && matchesStatus
        }
    }
}
