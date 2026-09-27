package com.platform.app.domain.model

data class Bill(
    val id: String,
    val title: String,
    val description: String = "",
    val type: BillType,
    val totalAmountCents: Long,
    val categoryId: String? = null,
    val totalInstallments: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
