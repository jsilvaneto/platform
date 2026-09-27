package com.platform.app.presentation.categories

import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.Category

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
