package com.platform.app.domain.model

data class FinancialAccount(
    val id: String,
    val name: String,
    val accountType: FinancialAccountType = FinancialAccountType.CORRENTE,
    val colorHex: String = "#2563EB",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
