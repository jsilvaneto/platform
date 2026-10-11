package com.platform.app.domain.model

data class Category(
    val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String = "folder",
    val nature: ExpenseNature = ExpenseNature.NECESSARIO,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

