package com.platform.app.presentation.home

import com.platform.app.core.mvi.UiAction
import com.platform.app.domain.model.PlatformItem

sealed interface HomeUiAction : UiAction {
    data class AddItem(val title: String, val description: String) : HomeUiAction
    data class ToggleItemCompletion(val item: PlatformItem) : HomeUiAction
    data class DeleteItem(val id: String) : HomeUiAction
    data class SearchQueryChanged(val query: String) : HomeUiAction
    data class FilterCompletedChanged(val filterCompleted: Boolean?) : HomeUiAction
    object ClearCompleted : HomeUiAction
    object Refresh : HomeUiAction
}
