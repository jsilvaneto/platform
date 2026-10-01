package com.platform.app.presentation.recurring

import app.cash.turbine.test
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.ToggleInstallmentPaymentUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringInstallmentsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FinancialRepository
    private lateinit var togglePaymentUseCase: ToggleInstallmentPaymentUseCase
    private lateinit var viewModel: RecurringInstallmentsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        togglePaymentUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData correctly identifies variable first installment and calculates real recurring rate`() = runTest {
        val billId = "bill-recurring-1"
        val recurringBill = Bill(
            id = billId,
            title = "Pós Graduação",
            type = BillType.RECURRING,
            totalAmountCents = 200000L,
            contactId = "contact-1",
            totalInstallments = 12
        )

        val installments = listOf(
            BillInstallment(
                id = "inst-1",
                billId = billId,
                billTitle = "Pós Graduação",
                installmentNumber = 1,
                totalInstallments = 12,
                amountCents = 200000L, // Entrada / Adesão de 2000
                dueDate = System.currentTimeMillis() + 86400000L,
                status = BillStatus.PENDING,
                type = BillType.RECURRING,
                contactName = "Universidade Católica",
                categoryName = "Educação"
            ),
            BillInstallment(
                id = "inst-2",
                billId = billId,
                billTitle = "Pós Graduação",
                installmentNumber = 2,
                totalInstallments = 12,
                amountCents = 35000L, // Mensalidade regular de 350
                dueDate = System.currentTimeMillis() + 86400000L * 30,
                status = BillStatus.PENDING,
                type = BillType.RECURRING,
                contactName = "Universidade Católica",
                categoryName = "Educação"
            ),
            BillInstallment(
                id = "inst-3",
                billId = billId,
                billTitle = "Pós Graduação",
                installmentNumber = 3,
                totalInstallments = 12,
                amountCents = 35000L,
                dueDate = System.currentTimeMillis() + 86400000L * 60,
                status = BillStatus.PENDING,
                type = BillType.RECURRING,
                contactName = "Universidade Católica",
                categoryName = "Educação"
            )
        )

        every { repository.getBills() } returns flowOf(listOf(recurringBill))
        every { repository.getAllInstallments() } returns flowOf(installments)

        viewModel = RecurringInstallmentsViewModel(repository, togglePaymentUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)

        val item = state.items.first()
        // Contato e categoria propagados
        assertEquals("Universidade Católica", item.contactName)
        assertEquals("Educação", item.categoryName)

        // Verificação da inteligência de recorrência:
        // A mensalidade regular deve ser 350,00 e NÃO 2000,00
        assertEquals(35000L, item.regularAmountCents)
        assertTrue(item.hasVariableFirstInstallment)
        assertEquals(200000L, item.firstInstallmentAmountCents)

        // Compromisso Mensal Fixo no resumo deve somar a mensalidade real regular
        assertEquals(35000L, state.totalMonthlyRecurringCents)
    }

    @Test
    fun `search by contact name filters planned payments properly`() = runTest {
        val billId1 = "bill-1"
        val billId2 = "bill-2"

        val bill1 = Bill(
            id = billId1,
            title = "Curso de Inglês",
            type = BillType.RECURRING,
            totalAmountCents = 25000L
        )
        val bill2 = Bill(
            id = billId2,
            title = "Plano de Saúde",
            type = BillType.RECURRING,
            totalAmountCents = 45000L
        )

        val inst1 = BillInstallment(
            id = "inst-1",
            billId = billId1,
            billTitle = "Curso de Inglês",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 25000L,
            dueDate = System.currentTimeMillis(),
            type = BillType.RECURRING,
            contactName = "Escola Britânica"
        )
        val inst2 = BillInstallment(
            id = "inst-2",
            billId = billId2,
            billTitle = "Plano de Saúde",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 45000L,
            dueDate = System.currentTimeMillis(),
            type = BillType.RECURRING,
            contactName = "Unimed"
        )

        every { repository.getBills() } returns flowOf(listOf(bill1, bill2))
        every { repository.getAllInstallments() } returns flowOf(listOf(inst1, inst2))

        viewModel = RecurringInstallmentsViewModel(repository, togglePaymentUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.filteredItems.size)

        // Filtrando por nome do contato "Britânica"
        viewModel.onAction(RecurringInstallmentsUiAction.SearchQueryChanged("Britânica"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredItems.size)
        assertEquals("Curso de Inglês", viewModel.uiState.value.filteredItems.first().bill.title)
        assertEquals("Escola Britânica", viewModel.uiState.value.filteredItems.first().contactName)
    }
}
