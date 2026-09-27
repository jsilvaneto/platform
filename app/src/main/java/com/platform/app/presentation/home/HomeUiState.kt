package com.platform.app.presentation.home

import com.platform.app.domain.model.PlatformItem

data class HomeUiState(
    val items: List<PlatformItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
