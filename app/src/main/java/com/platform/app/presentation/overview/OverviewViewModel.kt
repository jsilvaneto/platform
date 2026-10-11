package com.platform.app.presentation.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.usecase.GetPortfolioOverviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    getPortfolioOverviewUseCase: GetPortfolioOverviewUseCase
) : ViewModel() {

    val uiState: StateFlow<OverviewUiState> = getPortfolioOverviewUseCase()
        .map { OverviewUiState(isLoading = false, overview = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OverviewUiState(isLoading = true)
        )
}
