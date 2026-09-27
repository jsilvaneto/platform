package com.platform.app.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.model.Category
import com.platform.app.domain.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialCategoriesIfEmpty()
        }
        observeCategories()
    }

    private fun observeCategories() {
        _uiState.update { it.copy(isLoading = true) }

        repository.getCategories()
            .onEach { categories ->
                _uiState.update {
                    it.copy(
                        categories = categories,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Erro ao carregar categorias."
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun addCategory(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newCategory = Category(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                colorHex = colorHex
            )
            repository.saveCategory(newCategory)
        }
    }
}
