package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.ExpenseNature

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val nature: String = "NECESSARIO"
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            colorHex = colorHex,
            iconName = iconName,
            nature = try {
                ExpenseNature.valueOf(nature)
            } catch (e: Exception) {
                ExpenseNature.NECESSARIO
            }
        )
    }

    companion object {
        fun fromDomain(domain: Category): CategoryEntity {
            return CategoryEntity(
                id = domain.id,
                name = domain.name,
                colorHex = domain.colorHex,
                iconName = domain.iconName,
                nature = domain.nature.name
            )
        }
    }
}
