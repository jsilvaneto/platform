package com.platform.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.domain.model.DrawerOverview
import com.platform.app.domain.usecase.GetDrawerOverviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getDrawerOverviewUseCase: GetDrawerOverviewUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val drawerOverview: StateFlow<DrawerOverview> = getDrawerOverviewUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DrawerOverview()
        )

    fun setInitialScreen(screen: String) {
        viewModelScope.launch {
            preferencesManager.setInitialScreen(screen)
        }
    }
}
