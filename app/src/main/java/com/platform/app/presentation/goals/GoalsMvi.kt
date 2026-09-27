package com.platform.app.presentation.goals

import com.platform.app.core.mvi.UiAction
import com.platform.app.core.mvi.UiEffect
import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.Goal

data class GoalsUiState(
    val goals: List<Goal> = emptyList(),
    val totalTargetCents: Long = 0L,
    val totalSavedCents: Long = 0L,
    val overallProgress: Float = 0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface GoalsUiAction : UiAction {
    data class SaveGoal(val goal: Goal) : GoalsUiAction
    data class AddContribution(val goalId: String, val amountCents: Long) : GoalsUiAction
    data class DeleteGoal(val goalId: String) : GoalsUiAction
    object Refresh : GoalsUiAction
}

sealed interface GoalsUiEffect : UiEffect {
    data class ShowSnackbar(val message: String) : GoalsUiEffect
    object GoalSaved : GoalsUiEffect
}
