package com.platform.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.model.PlatformItem
import com.platform.app.domain.repository.ItemRepository
import com.platform.app.domain.usecase.GetItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getItemsUseCase: GetItemsUseCase,
    private val repository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeItems()
        syncRemoteData()
    }

    fun observeItems() {
        viewModelScope.launch {
            getItemsUseCase()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.localizedMessage ?: "Erro desconhecido ao carregar itens."
                        )
                    }
                }
                .collect { items ->
                    _uiState.update {
                        it.copy(
                            items = items,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun syncRemoteData() {
        viewModelScope.launch {
            repository.syncRemoteItems()
        }
    }

    fun addItem(title: String, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newItem = PlatformItem(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                description = description.trim(),
                isCompleted = false
            )
            repository.saveItem(newItem)
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.deleteItem(id)
        }
    }
}
