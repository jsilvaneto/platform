package com.platform.app.presentation.bills

import androidx.lifecycle.SavedStateHandle
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.CalculateInstallmentsUseCase
import io.mockk.coEvery
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
class NewExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FinancialRepository
    private lateinit var calculateInstallmentsUseCase: CalculateInstallmentsUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        calculateInstallmentsUseCase = mockk(relaxed = true)

        every { repository.getCategories() } returns flowOf(emptyList())
        every { repository.getExpenseItems() } returns flowOf(emptyList())
        every { repository.getContacts() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())
        every { repository.getPaymentMethods() } returns flowOf(emptyList())
        every { repository.getCreditCards() } returns flowOf(emptyList())
        every { repository.getAllInstallments() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `When duplicateBillId is provided, form fields are pre-populated with original bill values`() = runTest {
        val originalBill = Bill(
            id = "bill-orig-1",
            title = "Assinatura Antivirus",
            description = "Renovação anual da licença",
            type = BillType.SINGLE,
            totalAmountCents = 15990L,
            categoryId = "cat-tech",
            itemId = "item-soft",
            contactId = "contact-vendor",
            financialAccountId = "acc-bank",
            paymentMethodId = "pm-pix",
            totalInstallments = 1,
            createdAt = System.currentTimeMillis()
        )

        coEvery { repository.getBillById("bill-orig-1") } returns originalBill

        val savedStateHandle = SavedStateHandle(mapOf("duplicateBillId" to "bill-orig-1"))
        val viewModel = NewExpenseViewModel(
            savedStateHandle = savedStateHandle,
            repository = repository,
            calculateInstallmentsUseCase = calculateInstallmentsUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Renovação anual da licença", state.description)
        assertEquals(15990L, state.amountCents)
        assertEquals("cat-tech", state.selectedCategoryId)
        assertEquals("item-soft", state.selectedItemId)
        assertEquals("contact-vendor", state.selectedContactId)
        assertEquals("acc-bank", state.selectedFinancialAccountId)
        assertEquals("pm-pix", state.selectedPaymentMethodId)
        assertEquals(BillType.SINGLE, state.expenseType)
    }

    @Test
    fun `When duplicateBillId is null, form fields are initialized with default values`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = NewExpenseViewModel(
            savedStateHandle = savedStateHandle,
            repository = repository,
            calculateInstallmentsUseCase = calculateInstallmentsUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.description)
        assertEquals(0L, state.amountCents)
        assertEquals(BillType.SINGLE, state.expenseType)
    }
}
