package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Subcategory

@Entity(
    tableName = "subcategories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId")
    ]
)
data class SubcategoryEntity(
    @PrimaryKey
    val id: String,
    val categoryId: String,
    val name: String
) {
    fun toDomain(): Subcategory {
        return Subcategory(
            id = id,
            categoryId = categoryId,
            name = name
        )
    }

    companion object {
        fun fromDomain(sub: Subcategory): SubcategoryEntity {
            return SubcategoryEntity(
                id = sub.id,
                categoryId = sub.categoryId,
                name = sub.name
            )
        }
    }
}
