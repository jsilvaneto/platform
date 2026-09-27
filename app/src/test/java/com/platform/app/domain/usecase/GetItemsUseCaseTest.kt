package com.platform.app.domain.usecase

import com.platform.app.domain.model.PlatformItem
import com.platform.app.domain.repository.ItemRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetItemsUseCaseTest {

    private lateinit var repository: ItemRepository
    private lateinit var getItemsUseCase: GetItemsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        getItemsUseCase = GetItemsUseCase(repository)
    }

    @Test
    fun `invoke should return flow of items from repository`() = runTest {
        // Given
        val expectedItems = listOf(
            PlatformItem(id = "1", title = "Item 1", description = "Desc 1"),
            PlatformItem(id = "2", title = "Item 2", description = "Desc 2")
        )
        every { repository.getItems() } returns flowOf(expectedItems)

        // When
        val result = getItemsUseCase().first()

        // Then
        assertEquals(expectedItems, result)
        verify(exactly = 1) { repository.getItems() }
    }
}
