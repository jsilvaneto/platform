package com.platform.app.presentation.creditcards

import app.cash.turbine.test
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.GetCreditCardSummariesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreditCardsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FinancialRepository
    private lateinit var getCreditCardSummariesUseCase: GetCreditCardSummariesUseCase
    private lateinit var viewModel: CreditCardsViewModel

    private val sampleCard1 = CreditCard(
        id = "card-1",
        name = "Nubank Ultravioleta",
        totalLimitCents = 1500000L,
        closingDay = 25,
        dueDay = 5
    )

    private val sampleCard2 = CreditCard(
        id = "card-2",
        name = "Itaú Personnalité",
        totalLimitCents = 3000000L,
        closingDay = 10,
        dueDay = 20
    )

    private val sampleInvoice1 = CreditCardInvoice(
        id = "inv-1",
        creditCardId = "card-1",
        referenceMonth = "2026-10",
        closingDate = 1761350400000L,
        dueDate = 1762300800000L,
        status = InvoiceStatus.ABERTA,
        totalAmountCents = 450000L
    )

    private val sampleSummary1 = CreditCardWithInvoiceSummary(
        card = sampleCard1,
        currentInvoice = sampleInvoice1,
        usedLimitCents = 450000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        getCreditCardSummariesUseCase = mockk(relaxed = true)

        every { getCreditCardSummariesUseCase() } returns flowOf(listOf(sampleSummary1))
        every { repository.getAllCreditCardInvoices() } returns flowOf(listOf(sampleInvoice1))
        every { repository.getInstallmentsForInvoice(any()) } returns flowOf(emptyList())

        viewModel = CreditCardsViewModel(repository, getCreditCardSummariesUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates cards and sets active invoice`() = runTest {
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.cardsWithSummary.size)
            assertEquals("card-1", state.selectedCardId)
            assertEquals(sampleInvoice1, state.activeInvoice)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SelectCard updates selectedCardId`() = runTest {
        advanceUntilIdle()

        viewModel.onAction(CreditCardsUiAction.SelectCard("card-2"))
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("card-2", state.selectedCardId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SetInvoiceFilter filters invoices appropriately`() = runTest {
        advanceUntilIdle()

        viewModel.onAction(CreditCardsUiAction.SetInvoiceFilter(InvoiceFilter.OPEN))
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(InvoiceFilter.OPEN, state.invoiceFilter)
            assertEquals(1, state.filteredInvoices.size)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onAction(CreditCardsUiAction.SetInvoiceFilter(InvoiceFilter.PAID))
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(InvoiceFilter.PAID, state.invoiceFilter)
            assertEquals(0, state.filteredInvoices.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SelectInvoiceForDetails updates selection and active invoice`() = runTest {
        advanceUntilIdle()

        val closedInvoice = sampleInvoice1.copy(id = "inv-closed", status = InvoiceStatus.FECHADA)
        viewModel.onAction(CreditCardsUiAction.SelectInvoiceForDetails(closedInvoice))
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("inv-closed", state.activeInvoice?.id)
            assertEquals(InvoiceStatus.FECHADA, state.activeInvoice?.status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `PayInvoice delegates to repository`() = runTest {
        advanceUntilIdle()

        viewModel.onAction(CreditCardsUiAction.PayInvoice("inv-1", 1762300800000L))
        advanceUntilIdle()

        coVerify { repository.payInvoice("inv-1", 1762300800000L) }
    }

    @Test
    fun `SaveCard delegates to repository`() = runTest {
        advanceUntilIdle()

        viewModel.onAction(CreditCardsUiAction.SaveCard(sampleCard2))
        advanceUntilIdle()

        coVerify { repository.saveCreditCard(sampleCard2) }
    }

    @Test
    fun `ConfirmDeleteCard deletes card from repository`() = runTest {
        advanceUntilIdle()

        viewModel.onAction(CreditCardsUiAction.RequestDeleteCard("card-1"))
        advanceUntilIdle()
        viewModel.onAction(CreditCardsUiAction.ConfirmDeleteCard)
        advanceUntilIdle()

        coVerify { repository.deleteCreditCard("card-1") }
    }
}
