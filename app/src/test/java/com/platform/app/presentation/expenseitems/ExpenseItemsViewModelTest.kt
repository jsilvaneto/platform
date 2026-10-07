package com.platform.app.presentation.expenseitems

import com.platform.app.domain.model.Category
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseItemsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FinancialRepository
    private val expenseItemsFlow = MutableStateFlow<List<ExpenseItem>>(emptyList())
    private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.getExpenseItems() } returns expenseItemsFlow
        every { repository.getCategories() } returns categoriesFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load data into state`() = runTest {
        val cat1 = Category(id = "cat-1", name = "Alimentação", colorHex = "#10B981", nature = ExpenseNature.OBRIGATORIO)
        val item1 = ExpenseItem(id = "item-1", name = "Supermercado", categoryId = "cat-1", categoryName = "Alimentação", nature = ExpenseNature.OBRIGATORIO)

        categoriesFlow.value = listOf(cat1)
        expenseItemsFlow.value = listOf(item1)

        val viewModel = ExpenseItemsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals("Supermercado", viewModel.uiState.value.items[0].name)
        assertEquals(1, viewModel.uiState.value.filteredItems.size)
    }

    @Test
    fun `SearchQueryChanged should filter items by name and category`() = runTest {
        val cat1 = Category(id = "cat-1", name = "Alimentação", colorHex = "#10B981")
        val cat2 = Category(id = "cat-2", name = "Transporte", colorHex = "#3B82F6")

        val item1 = ExpenseItem(id = "item-1", name = "Supermercado", categoryId = "cat-1", categoryName = "Alimentação")
        val item2 = ExpenseItem(id = "item-2", name = "Combustível", categoryId = "cat-2", categoryName = "Transporte")
        val item3 = ExpenseItem(id = "item-3", name = "Padaria", categoryId = "cat-1", categoryName = "Alimentação")

        categoriesFlow.value = listOf(cat1, cat2)
        expenseItemsFlow.value = listOf(item1, item2, item3)

        val viewModel = ExpenseItemsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ExpenseItemsUiAction.SearchQueryChanged("super"))
        assertEquals(1, viewModel.uiState.value.filteredItems.size)
        assertEquals("Supermercado", viewModel.uiState.value.filteredItems[0].name)

        viewModel.onAction(ExpenseItemsUiAction.SearchQueryChanged("Transporte"))
        assertEquals(1, viewModel.uiState.value.filteredItems.size)
        assertEquals("Combustível", viewModel.uiState.value.filteredItems[0].name)

        viewModel.onAction(ExpenseItemsUiAction.SearchQueryChanged(""))
        assertEquals(3, viewModel.uiState.value.filteredItems.size)
    }

    @Test
    fun `CategoryFilterChanged should filter items by categoryId`() = runTest {
        val cat1 = Category(id = "cat-1", name = "Alimentação", colorHex = "#10B981")
        val cat2 = Category(id = "cat-2", name = "Transporte", colorHex = "#3B82F6")

        val item1 = ExpenseItem(id = "item-1", name = "Supermercado", categoryId = "cat-1", categoryName = "Alimentação")
        val item2 = ExpenseItem(id = "item-2", name = "Combustível", categoryId = "cat-2", categoryName = "Transporte")

        categoriesFlow.value = listOf(cat1, cat2)
        expenseItemsFlow.value = listOf(item1, item2)

        val viewModel = ExpenseItemsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ExpenseItemsUiAction.CategoryFilterChanged("cat-1"))
        assertEquals(1, viewModel.uiState.value.filteredItems.size)
        assertEquals("Supermercado", viewModel.uiState.value.filteredItems[0].name)

        viewModel.onAction(ExpenseItemsUiAction.CategoryFilterChanged(null))
        assertEquals(2, viewModel.uiState.value.filteredItems.size)
    }

    @Test
    fun `SaveItem should invoke repository saveExpenseItem for new or edited item`() = runTest {
        val viewModel = ExpenseItemsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val editedItem = ExpenseItem(
            id = "existing-item-id",
            name = "Supermercado Premium",
            categoryId = "cat-1",
            categoryName = "Alimentação",
            nature = ExpenseNature.OBRIGATORIO
        )

        viewModel.onAction(ExpenseItemsUiAction.SaveItem(editedItem))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.saveExpenseItem(editedItem) }
    }

    @Test
    fun `DeleteItem should invoke repository deleteExpenseItem`() = runTest {
        val viewModel = ExpenseItemsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ExpenseItemsUiAction.DeleteItem("item-to-delete"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.deleteExpenseItem("item-to-delete") }
    }
}
