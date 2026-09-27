package com.platform.app.domain.model

data class Budget(
    val id: String,
    val categoryId: String? = null,
    val categoryName: String = "Geral",
    val limitAmountCents: Long,
    val colorHex: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis()
)
