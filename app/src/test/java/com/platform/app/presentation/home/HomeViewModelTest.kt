package com.platform.app.presentation.home

import app.cash.turbine.test
import com.platform.app.domain.model.PlatformItem
import com.platform.app.domain.repository.ItemRepository
import com.platform.app.domain.usecase.GetItemsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getItemsUseCase: GetItemsUseCase
    private lateinit var repository: ItemRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getItemsUseCase = mockk()
        repository = mockk(relaxed = true)

        coEvery { repository.syncRemoteItems() } returns Result.success(Unit)
        every { getItemsUseCase() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should emit loaded items correctly`() = runTest {
        val mockItems = listOf(
            PlatformItem(id = "1", title = "Task A", description = "Details A")
        )
        every { getItemsUseCase() } returns flowOf(mockItems)

        viewModel = HomeViewModel(getItemsUseCase, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(mockItems, state.items)
            assertEquals(false, state.isLoading)
        }
    }

    @Test
    fun `addItem should call repository saveItem`() = runTest {
        viewModel = HomeViewModel(getItemsUseCase, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addItem("Nova Tarefa", "Descricao da Tarefa")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.saveItem(any()) }
    }
}
