package com.platform.app.presentation.bills

import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.Category

data class BillsUiState(
    val selectedMonthMillis: Long = System.currentTimeMillis(),
    val installments: List<BillInstallment> = emptyList(),
    val filteredInstallments: List<BillInstallment> = emptyList(),
    val categories: List<Category> = emptyList(),
    val typeFilter: BillType? = null,
    val statusFilter: BillStatus? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
