package com.platform.app.domain.model

import java.util.UUID

data class ExpenseItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val categoryId: String,
    val categoryName: String = "",
    val categoryColorHex: String = "#64748B",
    val categoryIconName: String = "category",
    val nature: ExpenseNature = ExpenseNature.NECESSARIO,
    val syncStatus: String = "PENDENTE"
)
