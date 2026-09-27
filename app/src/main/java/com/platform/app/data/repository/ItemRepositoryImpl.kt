package com.platform.app.data.repository

import com.platform.app.data.local.dao.ItemDao
import com.platform.app.data.local.entity.ItemEntity
import com.platform.app.domain.model.PlatformItem
import com.platform.app.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val dao: ItemDao
) : ItemRepository {

    override fun getItems(): Flow<List<PlatformItem>> {
        return dao.getAllItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getItemById(id: String): PlatformItem? {
        return dao.getItemById(id)?.toDomain()
    }

    override suspend fun saveItem(item: PlatformItem) {
        dao.insertItem(ItemEntity.fromDomain(item))
    }

    override suspend fun deleteItem(id: String) {
        dao.deleteById(id)
    }

    override suspend fun toggleItemCompletion(id: String) {
        dao.toggleCompletion(id)
    }

    override suspend fun clearCompletedItems() {
        dao.deleteCompleted()
    }
}
