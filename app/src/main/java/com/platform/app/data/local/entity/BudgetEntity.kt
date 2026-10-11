package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Budget

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    val categoryId: String?,
    val categoryName: String,
    val limitAmountCents: Long,
    val colorHex: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val deletedAt: Long? = null
) {
    fun toDomain(): Budget = Budget(
        id = id,
        categoryId = categoryId,
        categoryName = categoryName,
        limitAmountCents = limitAmountCents,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )

    companion object {
        fun fromDomain(budget: Budget): BudgetEntity = BudgetEntity(
            id = budget.id,
            categoryId = budget.categoryId,
            categoryName = budget.categoryName,
            limitAmountCents = budget.limitAmountCents,
            colorHex = budget.colorHex,
            createdAt = budget.createdAt,
            updatedAt = budget.updatedAt,
            deletedAt = budget.deletedAt
        )
    }
}
