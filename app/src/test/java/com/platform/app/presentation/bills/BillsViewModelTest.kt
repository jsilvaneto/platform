package com.platform.app.presentation.bills

import app.cash.turbine.test
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.ToggleInstallmentPaymentUseCase
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
class BillsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FinancialRepository
    private lateinit var togglePaymentUseCase: ToggleInstallmentPaymentUseCase
    private lateinit var viewModel: BillsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        togglePaymentUseCase = mockk(relaxed = true)

        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(emptyList())
        every { repository.getAllInstallments() } returns flowOf(emptyList())
        every { repository.getCategories() } returns flowOf(emptyList())
        every { repository.getExpenseItems() } returns flowOf(emptyList())
        every { repository.getContacts() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())
        every { repository.getPaymentMethods() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SaveInstallmentEdit should invoke updateBillAndInstallment and emit ShowSnackbar`() = runTest {
        viewModel = BillsViewModel(repository, togglePaymentUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(
                BillsUiAction.SaveInstallmentEdit(
                    installmentId = "inst-1",
                    billId = "bill-1",
                    title = "Aluguel Atualizado",
                    description = "Mensalidade",
                    amountCents = 260000L,
                    dueDate = 1759000000000L,
                    categoryId = "cat-1",
                    itemId = null,
                    contactId = null,
                    financialAccountId = null,
                    paymentMethodId = null
                )
            )
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) {
                repository.updateBillAndInstallment(
                    installmentId = "inst-1",
                    billId = "bill-1",
                    title = "Aluguel Atualizado",
                    description = "Mensalidade",
                    amountCents = 260000L,
                    dueDate = 1759000000000L,
                    categoryId = "cat-1",
                    itemId = null,
                    contactId = null,
                    financialAccountId = null,
                    paymentMethodId = null
                )
            }

            val effect = awaitItem()
            assertTrue(effect is BillsUiEffect.ShowSnackbar)
            assertTrue((effect as BillsUiEffect.ShowSnackbar).message.contains("sucesso"))
        }
    }

    @Test
    fun `TogglePayment action should invoke togglePaymentUseCase and emit ShowSnackbar`() = runTest {
        val installment = BillInstallment(
            id = "inst-1",
            billId = "bill-1",
            billTitle = "Conta de Luz",
            categoryId = null,
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 12000L,
            dueDate = System.currentTimeMillis(),
            status = BillStatus.PENDING,
            type = BillType.SINGLE
        )

        viewModel = BillsViewModel(repository, togglePaymentUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(BillsUiAction.TogglePayment(installment))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { togglePaymentUseCase("inst-1", false, null) }

            val effect = awaitItem()
            assertTrue(effect is BillsUiEffect.ShowSnackbar)
        }
    }

    @Test
    fun `TogglePayment action with actualPaymentDate should forward date to togglePaymentUseCase`() = runTest {
        val installment = BillInstallment(
            id = "inst-1",
            billId = "bill-1",
            billTitle = "Conta de Luz",
            categoryId = null,
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 12000L,
            dueDate = 1759000000000L,
            status = BillStatus.PENDING,
            type = BillType.SINGLE
        )
        val customPaymentDate = 1759000000000L

        viewModel = BillsViewModel(repository, togglePaymentUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(BillsUiAction.TogglePayment(installment, customPaymentDate))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { togglePaymentUseCase("inst-1", false, customPaymentDate) }

            val effect = awaitItem()
            assertTrue(effect is BillsUiEffect.ShowSnackbar)
        }
    }

    @Test
    fun `SearchQueryChanged should filter installments by title`() = runTest {
        val inst1 = BillInstallment(
            id = "1", billId = "b1", billTitle = "Internet Fibra", categoryId = null,
            installmentNumber = 1, totalInstallments = 1, amountCents = 10000L,
            dueDate = System.currentTimeMillis()
        )
        val inst2 = BillInstallment(
            id = "2", billId = "b2", billTitle = "Supermercado", categoryId = null,
            installmentNumber = 1, totalInstallments = 1, amountCents = 35000L,
            dueDate = System.currentTimeMillis()
        )
        every { repository.getAllInstallments() } returns flowOf(listOf(inst1, inst2))

        viewModel = BillsViewModel(repository, togglePaymentUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(BillsUiAction.SearchQueryChanged("Internet"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredInstallments.size)
        assertEquals("Internet Fibra", viewModel.uiState.value.filteredInstallments[0].billTitle)
    }
}
