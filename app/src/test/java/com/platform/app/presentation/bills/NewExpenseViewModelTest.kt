package com.platform.app.presentation.bills

import androidx.lifecycle.SavedStateHandle
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.CreateBillUseCase
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
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FinancialRepository
    private lateinit var createBillUseCase: CreateBillUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        createBillUseCase = mockk(relaxed = true)

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
            createBillUseCase = createBillUseCase
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
            createBillUseCase = createBillUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.description)
        assertEquals(0L, state.amountCents)
        assertEquals(BillType.SINGLE, state.expenseType)
        assertEquals(RecurrenceFrequency.MONTHLY, state.recurrenceFrequency)
        assertEquals(RecurrenceEndType.FOREVER, state.recurrenceEndType)
    }

    @Test
    fun `When selecting payment method Cartao de Debito, isCreditCard must be false`() = runTest {
        val debitMethod = PaymentMethod(id = "pm-debit", name = "Cartão de Débito", iconName = "credit_card")
        val creditMethod = PaymentMethod(id = "pm-credit", name = "Cartão de Crédito", iconName = "credit_card")
        every { repository.getPaymentMethods() } returns flowOf(listOf(debitMethod, creditMethod))

        val viewModel = NewExpenseViewModel(
            savedStateHandle = SavedStateHandle(),
            repository = repository,
            createBillUseCase = createBillUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Selecionar Cartão de Débito
        viewModel.onPaymentMethodSelect(debitMethod.id)

        val state = viewModel.uiState.value
        assertEquals("pm-debit", state.selectedPaymentMethodId)
        assertFalse("Selecionar débito não deve marcar opção de cartão de crédito", state.isCreditCard)
    }

    @Test
    fun `When selecting payment method Cartao de Credito, isCreditCard must be true`() = runTest {
        val debitMethod = PaymentMethod(id = "pm-debit", name = "Cartão de Débito", iconName = "credit_card")
        val creditMethod = PaymentMethod(id = "pm-credit", name = "Cartão de Crédito", iconName = "credit_card")
        every { repository.getPaymentMethods() } returns flowOf(listOf(debitMethod, creditMethod))

        val viewModel = NewExpenseViewModel(
            savedStateHandle = SavedStateHandle(),
            repository = repository,
            createBillUseCase = createBillUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Selecionar Cartão de Crédito
        viewModel.onPaymentMethodSelect(creditMethod.id)

        val state = viewModel.uiState.value
        assertEquals("pm-credit", state.selectedPaymentMethodId)
        assertTrue("Selecionar crédito deve marcar opção de cartão de crédito", state.isCreditCard)

        // Alternar de volta para Cartão de Débito
        viewModel.onPaymentMethodSelect(debitMethod.id)
        val stateAfterDebit = viewModel.uiState.value
        assertEquals("pm-debit", stateAfterDebit.selectedPaymentMethodId)
        assertFalse("Ao mudar para débito, isCreditCard deve voltar a ser false", stateAfterDebit.isCreditCard)
    }

    @Test
    fun `When recurrence settings are changed, state correctly updates frequency, endType and occurrences`() = runTest {
        val viewModel = NewExpenseViewModel(
            savedStateHandle = SavedStateHandle(),
            repository = repository,
            createBillUseCase = createBillUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onExpenseTypeChange(BillType.RECURRING)
        viewModel.onRecurrenceFrequencyChange(RecurrenceFrequency.WEEKLY)
        viewModel.onRecurrenceEndTypeChange(RecurrenceEndType.BY_OCCURRENCES)
        viewModel.onRecurrenceOccurrencesCountChange(8)

        val state = viewModel.uiState.value
        assertEquals(BillType.RECURRING, state.expenseType)
        assertEquals(RecurrenceFrequency.WEEKLY, state.recurrenceFrequency)
        assertEquals(RecurrenceEndType.BY_OCCURRENCES, state.recurrenceEndType)
        assertEquals(8, state.recurrenceOccurrencesCount)
    }

    @Test
    fun `When duplicating recurring bill, recurrence settings are loaded into state`() = runTest {
        val originalBill = Bill(
            id = "bill-rec-1",
            title = "Assinatura Semanal",
            description = "Café da semana",
            type = BillType.RECURRING,
            totalAmountCents = 3500L,
            recurrenceFrequency = RecurrenceFrequency.WEEKLY,
            recurrenceEndType = RecurrenceEndType.BY_OCCURRENCES,
            totalInstallments = 10,
            createdAt = System.currentTimeMillis()
        )

        coEvery { repository.getBillById("bill-rec-1") } returns originalBill

        val viewModel = NewExpenseViewModel(
            savedStateHandle = SavedStateHandle(mapOf("duplicateBillId" to "bill-rec-1")),
            repository = repository,
            createBillUseCase = createBillUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(BillType.RECURRING, state.expenseType)
        assertEquals(RecurrenceFrequency.WEEKLY, state.recurrenceFrequency)
        assertEquals(RecurrenceEndType.BY_OCCURRENCES, state.recurrenceEndType)
        assertEquals(10, state.recurrenceOccurrencesCount)
    }
}
