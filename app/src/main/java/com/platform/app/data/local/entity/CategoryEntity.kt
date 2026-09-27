package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            colorHex = colorHex,
            iconName = iconName
        )
    }

    companion object {
        fun fromDomain(domain: Category): CategoryEntity {
            return CategoryEntity(
                id = domain.id,
                name = domain.name,
                colorHex = domain.colorHex,
                iconName = domain.iconName
            )
        }
    }
}
