package com.platform.app.presentation.home

import app.cash.turbine.test
import com.platform.app.core.connectivity.NetworkMonitor
import com.platform.app.domain.model.PlatformItem
import com.platform.app.domain.repository.ItemRepository
import com.platform.app.domain.usecase.GetItemsUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getItemsUseCase: GetItemsUseCase
    private lateinit var repository: ItemRepository
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getItemsUseCase = mockk()
        repository = mockk(relaxed = true)
        networkMonitor = mockk()

        every { networkMonitor.isOnline } returns flowOf(false)
        every { getItemsUseCase() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should emit loaded items and offline status correctly`() = runTest {
        val mockItems = listOf(
            PlatformItem(id = "1", title = "Tarefa Offline", description = "Salva no Room")
        )
        every { getItemsUseCase() } returns flowOf(mockItems)

        viewModel = HomeViewModel(getItemsUseCase, repository, networkMonitor)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(mockItems, state.items)
            assertEquals(mockItems, state.filteredItems)
            assertEquals(false, state.isLoading)
            assertTrue(state.isOfflineModeActive)
        }
    }

    @Test
    fun `onAction AddItem should save item locally and emit ShowSnackbar effect`() = runTest {
        viewModel = HomeViewModel(getItemsUseCase, repository, networkMonitor)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(HomeUiAction.AddItem("Nota Importante", "Conteudo da nota"))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { repository.saveItem(any()) }

            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.ShowSnackbar)
            assertTrue((effect as HomeUiEffect.ShowSnackbar).message.contains("Nota Importante"))
        }
    }

    @Test
    fun `onAction ToggleItemCompletion should toggle completion status in repository`() = runTest {
        val item = PlatformItem(id = "10", title = "Reunião", description = "", isCompleted = false)
        viewModel = HomeViewModel(getItemsUseCase, repository, networkMonitor)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(HomeUiAction.ToggleItemCompletion(item))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.toggleItemCompletion("10") }
    }

    @Test
    fun `onAction DeleteItem should delete item from repository and emit ShowSnackbar`() = runTest {
        viewModel = HomeViewModel(getItemsUseCase, repository, networkMonitor)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(HomeUiAction.DeleteItem("10"))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { repository.deleteItem("10") }

            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.ShowSnackbar)
        }
    }
}
