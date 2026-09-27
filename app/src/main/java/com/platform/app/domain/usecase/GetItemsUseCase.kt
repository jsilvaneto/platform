package com.platform.app.domain.usecase

import com.platform.app.domain.model.PlatformItem
import com.platform.app.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetItemsUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    operator fun invoke(): Flow<List<PlatformItem>> {
        return repository.getItems()
    }
}
