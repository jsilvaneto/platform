package com.platform.app.presentation.home

import app.cash.turbine.test
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.CalculateMonthlyForecastUseCase
import com.platform.app.domain.usecase.GetFinancialDashboardUseCase
import com.platform.app.domain.usecase.MonthlyForecastResult
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
    private lateinit var repository: FinancialRepository
    private lateinit var calculateMonthlyForecastUseCase: CalculateMonthlyForecastUseCase
    private lateinit var getFinancialDashboardUseCase: GetFinancialDashboardUseCase
    private lateinit var createBillUseCase: com.platform.app.domain.usecase.CreateBillUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        calculateMonthlyForecastUseCase = mockk(relaxed = true)
        getFinancialDashboardUseCase = mockk(relaxed = true)
        createBillUseCase = mockk(relaxed = true)

        val dummyForecast = MonthlyForecastResult(
            monthMillis = System.currentTimeMillis(),
            monthLabel = "Setembro 2026",
            totalForecastCents = 10000L,
            totalPaidCents = 0L
        )
        val dummyDashboard = FinancialDashboardMetrics(
            monthMillis = System.currentTimeMillis(),
            currentMonthLabel = "Setembro 2026"
        )

        every { calculateMonthlyForecastUseCase(any()) } returns flowOf(dummyForecast)
        every { getFinancialDashboardUseCase(any()) } returns flowOf(dummyDashboard)
        every { repository.getAllInstallments() } returns flowOf(emptyList())
        every { repository.getAllCreditCardInvoices() } returns flowOf(emptyList())
        every { repository.getCreditCards() } returns flowOf(emptyList())
        every { repository.getExpenseItems() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `PayBill action should toggle payment and emit ShowUndoSnackbar effect`() = runTest {
        viewModel = HomeViewModel(calculateMonthlyForecastUseCase, getFinancialDashboardUseCase, repository, createBillUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(HomeUiAction.PayBill("inst-123"))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) {
                repository.toggleInstallmentPayment(
                    installmentId = "inst-123",
                    isPaid = true,
                    paidTimestamp = any(),
                    actualPaymentDate = any()
                )
            }

            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.ShowUndoSnackbar)
            val undoEffect = effect as HomeUiEffect.ShowUndoSnackbar
            assertEquals(HomeUiAction.UndoPayBill("inst-123"), undoEffect.undoAction)
        }
    }

    @Test
    fun `UndoPayBill action should revert installment payment to unpaid`() = runTest {
        viewModel = HomeViewModel(calculateMonthlyForecastUseCase, getFinancialDashboardUseCase, repository, createBillUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(HomeUiAction.UndoPayBill("inst-123"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.toggleInstallmentPayment(
                installmentId = "inst-123",
                isPaid = false,
                paidTimestamp = null,
                actualPaymentDate = null
            )
        }
    }

    @Test
    fun `PayInvoice action should call payInvoice and emit ShowUndoSnackbar effect`() = runTest {
        viewModel = HomeViewModel(calculateMonthlyForecastUseCase, getFinancialDashboardUseCase, repository, createBillUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(HomeUiAction.PayInvoice("inv-456"))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { repository.payInvoice("inv-456") }

            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.ShowUndoSnackbar)
            val undoEffect = effect as HomeUiEffect.ShowUndoSnackbar
            assertEquals(HomeUiAction.UndoPayInvoice("inv-456"), undoEffect.undoAction)
        }
    }

    @Test
    fun `UndoPayInvoice action should reopen invoice in repository`() = runTest {
        viewModel = HomeViewModel(calculateMonthlyForecastUseCase, getFinancialDashboardUseCase, repository, createBillUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(HomeUiAction.UndoPayInvoice("inv-456"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.reopenInvoice("inv-456") }
    }

    @Test
    fun `SaveQuickExpense action should call createBillUseCase and emit ShowUndoSnackbar effect`() = runTest {
        io.mockk.coEvery { createBillUseCase.invoke(any(), any(), any(), any(), any()) } returns emptyList()
        viewModel = HomeViewModel(calculateMonthlyForecastUseCase, getFinancialDashboardUseCase, repository, createBillUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(HomeUiAction.SaveQuickExpense(amountCents = 4500L, itemId = "item-1", isPaid = false))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { createBillUseCase.invoke(any(), any(), any(), any(), any()) }

            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.ShowUndoSnackbar)
            val undoEffect = effect as HomeUiEffect.ShowUndoSnackbar
            assertTrue(undoEffect.undoAction is HomeUiAction.UndoSaveBill)
        }
    }

    @Test
    fun `UndoSaveBill action should call repository deleteBill`() = runTest {
        viewModel = HomeViewModel(calculateMonthlyForecastUseCase, getFinancialDashboardUseCase, repository, createBillUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(HomeUiAction.UndoSaveBill("bill-quick-123"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteBill("bill-quick-123") }
    }
}
