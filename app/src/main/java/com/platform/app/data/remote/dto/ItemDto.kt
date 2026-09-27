package com.platform.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.platform.app.domain.model.PlatformItem

data class ItemDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("is_completed")
    val isCompleted: Boolean = false,
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis()
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
}
