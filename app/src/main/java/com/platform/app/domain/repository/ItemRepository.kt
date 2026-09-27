package com.platform.app.domain.repository

import com.platform.app.domain.model.PlatformItem
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItems(): Flow<List<PlatformItem>>
    suspend fun getItemById(id: String): PlatformItem?
    suspend fun saveItem(item: PlatformItem)
    suspend fun deleteItem(id: String)
    suspend fun syncRemoteItems(): Result<Unit>
}
