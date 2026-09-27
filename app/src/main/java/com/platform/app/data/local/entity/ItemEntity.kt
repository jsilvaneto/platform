package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.PlatformItem

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val createdAt: Long
) {
    fun toDomain(): PlatformItem {
        return PlatformItem(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(item: PlatformItem): ItemEntity {
            return ItemEntity(
                id = item.id,
                title = item.title,
                description = item.description,
                isCompleted = item.isCompleted,
                createdAt = item.createdAt
            )
        }
    }
}
