package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sync_outbox",
    indices = [
        Index("status"),
        Index("createdAt"),
        Index(value = ["entityType", "entityId"])
    ]
)
data class SyncOutboxEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val entityType: String,
    val entityId: String,
    val operation: String, // INSERT, UPDATE, DELETE
    val payload: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, SYNCED, FAILED
    val retryCount: Int = 0,
    val errorMessage: String? = null
)
