package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.ExpenseNature
import java.util.UUID

@Entity(
    tableName = "expense_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoryId")]
)
data class ExpenseItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val categoryId: String,
    val syncStatus: String = "PENDENTE"
) {
    fun toDomain(categoryName: String = "", categoryColorHex: String = "#64748B", nature: ExpenseNature = ExpenseNature.NECESSARIO): ExpenseItem {
        return ExpenseItem(
            id = id,
            name = name,
            categoryId = categoryId,
            categoryName = categoryName,
            categoryColorHex = categoryColorHex,
            nature = nature,
            syncStatus = syncStatus
        )
    }

    companion object {
        fun fromDomain(item: ExpenseItem): ExpenseItemEntity {
            return ExpenseItemEntity(
                id = item.id,
                name = item.name,
                categoryId = item.categoryId,
                syncStatus = item.syncStatus
            )
        }
    }
}
