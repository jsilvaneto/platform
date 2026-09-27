package com.platform.app.presentation.home

import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.PlatformItem

data class HomeUiState(
    val items: List<PlatformItem> = emptyList(),
    val filteredItems: List<PlatformItem> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterCompleted: Boolean? = null,
    val isDeviceOnline: Boolean = false,
    val isOfflineModeActive: Boolean = true,
    val errorMessage: String? = null
) : UiState
