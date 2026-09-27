package com.platform.app.domain.model

data class PlatformItem(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
