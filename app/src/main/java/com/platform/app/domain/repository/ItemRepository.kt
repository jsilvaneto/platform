package com.platform.app.domain.repository

import com.platform.app.domain.model.PlatformItem
import kotlinx.coroutines.flow.Flow

/**
 * Repositório de dados operando com fonte de verdade 100% local (Offline-First).
 */
interface ItemRepository {
    fun getItems(): Flow<List<PlatformItem>>
    suspend fun getItemById(id: String): PlatformItem?
    suspend fun saveItem(item: PlatformItem)
    suspend fun deleteItem(id: String)
    suspend fun toggleItemCompletion(id: String)
    suspend fun clearCompletedItems()
}
