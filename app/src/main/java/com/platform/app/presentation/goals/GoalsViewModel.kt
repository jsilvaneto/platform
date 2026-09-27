package com.platform.app.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.model.Goal
import com.platform.app.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<GoalsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<GoalsUiEffect> = _effectChannel.receiveAsFlow()

    init {
        loadGoals()
    }

    fun onAction(action: GoalsUiAction) {
        when (action) {
            is GoalsUiAction.SaveGoal -> handleSaveGoal(action.goal)
            is GoalsUiAction.AddContribution -> handleAddContribution(action.goalId, action.amountCents)
            is GoalsUiAction.DeleteGoal -> handleDeleteGoal(action.goalId)
            is GoalsUiAction.Refresh -> loadGoals()
        }
    }

    private fun loadGoals() {
        _uiState.update { it.copy(isLoading = true) }
        repository.getGoals()
            .onEach { goals ->
                val totalTarget = goals.sumOf { it.targetAmountCents }
                val totalSaved = goals.sumOf { it.currentAmountCents }
                val progress = if (totalTarget > 0L) (totalSaved.toFloat() / totalTarget.toFloat()).coerceIn(0f, 1f) else 0f

                _uiState.update {
                    it.copy(
                        goals = goals,
                        totalTargetCents = totalTarget,
                        totalSavedCents = totalSaved,
                        overallProgress = progress,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Erro ao carregar metas."
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleSaveGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                repository.saveGoal(goal)
                _effectChannel.send(GoalsUiEffect.ShowSnackbar("Meta '${goal.name}' salva com sucesso!"))
                _effectChannel.send(GoalsUiEffect.GoalSaved)
            } catch (e: Exception) {
                _effectChannel.send(GoalsUiEffect.ShowSnackbar("Erro ao salvar meta: ${e.message}"))
            }
        }
    }

    private fun handleAddContribution(goalId: String, amountCents: Long) {
        viewModelScope.launch {
            try {
                repository.addContribution(goalId, amountCents)
                _effectChannel.send(GoalsUiEffect.ShowSnackbar("Aporte adicionado à meta!"))
            } catch (e: Exception) {
                _effectChannel.send(GoalsUiEffect.ShowSnackbar("Erro ao realizar aporte: ${e.message}"))
            }
        }
    }

    private fun handleDeleteGoal(goalId: String) {
        viewModelScope.launch {
            try {
                repository.deleteGoal(goalId)
                _effectChannel.send(GoalsUiEffect.ShowSnackbar("Meta excluída."))
            } catch (e: Exception) {
                _effectChannel.send(GoalsUiEffect.ShowSnackbar("Erro ao excluir meta: ${e.message}"))
            }
        }
    }
}
