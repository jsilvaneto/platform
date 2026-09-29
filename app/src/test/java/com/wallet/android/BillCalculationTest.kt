package com.wallet.android

import app.cash.turbine.test
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.CalculateMonthlyForecastUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class BillCalculationTest {

    private lateinit var repository: FinancialRepository
    private lateinit var calculateMonthlyForecastUseCase: CalculateMonthlyForecastUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        calculateMonthlyForecastUseCase = CalculateMonthlyForecastUseCase(repository)
    }

    @Test
    fun `should calculate total forecast summing pending standalone bills and open or closed credit card invoices`() = runTest {
        val now = System.currentTimeMillis()
        val startOfMonth = DateUtils.getStartOfMonth(now)
        val endOfMonth = DateUtils.getEndOfMonth(now)

        // Conta avulsa pendente no mês: R$ 150,00 (15000 centavos)
        val pendingBill1 = BillInstallment(
            id = "inst-1",
            billId = "bill-1",
            billTitle = "Energia Elétrica",
            amountCents = 15000L,
            dueDate = startOfMonth + 86400000L * 5, // Dia 6
            status = BillStatus.PENDING,
            invoiceId = null
        )

        // Conta avulsa pendente no mês: R$ 200,00 (20000 centavos)
        val pendingBill2 = BillInstallment(
            id = "inst-2",
            billId = "bill-2",
            billTitle = "Água e Saneamento",
            amountCents = 20000L,
            dueDate = startOfMonth + 86400000L * 10, // Dia 11
            status = BillStatus.PENDING,
            invoiceId = null
        )

        // Conta avulsa JÁ PAGA no mês: R$ 300,00 (não deve entrar no Total Previsto)
        val paidBill = BillInstallment(
            id = "inst-paid",
            billId = "bill-paid",
            billTitle = "Internet Fibra",
            amountCents = 30000L,
            dueDate = startOfMonth + 86400000L * 2,
            paidAt = startOfMonth + 86400000L * 2,
            status = BillStatus.PAID,
            invoiceId = null
        )

        // Compra no cartão vinculada à fatura: R$ 80,00 (não deve ser somada em dobro)
        val cardPurchase = BillInstallment(
            id = "inst-card",
            billId = "bill-card",
            billTitle = "Supermercado no Crédito",
            amountCents = 8000L,
            dueDate = startOfMonth + 86400000L * 15,
            status = BillStatus.PENDING,
            invoiceId = "inv-1"
        )

        val monthInstallments = listOf(pendingBill1, pendingBill2, paidBill, cardPurchase)

        // Cartão de crédito
        val card = CreditCard(
            id = "card-1",
            name = "Nubank",
            totalLimitCents = 500000L,
            closingDay = 25,
            dueDay = 5
        )

        // Fatura aberta/fechada que vence no mês: R$ 500,00 (50000 centavos)
        val openInvoice = CreditCardInvoice(
            id = "inv-1",
            creditCardId = "card-1",
            referenceMonth = "2026-09",
            closingDate = startOfMonth + 86400000L * 2,
            dueDate = startOfMonth + 86400000L * 15, // Vence no mês
            status = InvoiceStatus.FECHADA,
            totalAmountCents = 50000L
        )

        // Fatura de outro cartão JÁ PAGA no mês: R$ 120,00 (não entra no Total Previsto)
        val paidInvoice = CreditCardInvoice(
            id = "inv-paid",
            creditCardId = "card-1",
            referenceMonth = "2026-08",
            closingDate = startOfMonth - 86400000L * 10,
            dueDate = startOfMonth + 86400000L * 5,
            status = InvoiceStatus.PAGA,
            totalAmountCents = 12000L
        )

        val monthInvoices = listOf(openInvoice, paidInvoice)

        every { repository.getInstallmentsForPeriod(startOfMonth, endOfMonth) } returns flowOf(monthInstallments)
        every { repository.getCreditCards() } returns flowOf(listOf(card))
        every { repository.getInvoicesForPeriod(startOfMonth, endOfMonth) } returns flowOf(monthInvoices)

        calculateMonthlyForecastUseCase(startOfMonth, now).test {
            val result = awaitItem()

            // Total Previsto = 150,00 (bill1) + 200,00 (bill2) + 500,00 (fatura) = R$ 850,00 (85000 centavos)
            val expectedTotal = 15000L + 20000L + 50000L
            assertEquals(expectedTotal, result.totalForecastCents)

            // Total Já Pago no Mês = 300,00 (paidBill) + 120,00 (paidInvoice) = R$ 420,00 (42000 centavos)
            val expectedPaid = 30000L + 12000L
            assertEquals(expectedPaid, result.totalPaidCents)

            // Contas pendentes avulsas identificadas (2 contas) e fatura pendente identificada (1 fatura)
            val pendingBillsCount = (result.overdueItems + result.dueTodayItems + result.next7DaysItems + result.laterInMonthItems)
                .filterIsInstance<com.platform.app.domain.model.PayableItem.BillPayable>().size
            assertEquals(2, pendingBillsCount)

            val pendingInvoicesCount = (result.overdueItems + result.dueTodayItems + result.next7DaysItems + result.laterInMonthItems)
                .filterIsInstance<com.platform.app.domain.model.PayableItem.InvoicePayable>().size
            assertEquals(1, pendingInvoicesCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pure formula calculation should match exactly`() {
        val pendingBills = 35000L // R$ 350,00
        val openInvoices = 50000L // R$ 500,00

        val total = calculateMonthlyForecastUseCase.calculateTotalForecast(pendingBills, openInvoices)
        assertEquals(85000L, total)

        // Ao pagar uma conta de R$ 150,00 (15000), o total deduz instantaneamente
        val updatedTotal = calculateMonthlyForecastUseCase.calculateTotalForecast(pendingBills - 15000L, openInvoices)
        assertEquals(70000L, updatedTotal)

        // Ao liquidar a fatura inteira de R$ 500,00 (50000), o total deduz instantaneamente
        val afterInvoicePaid = calculateMonthlyForecastUseCase.calculateTotalForecast(20000L, 0L)
        assertEquals(20000L, afterInvoicePaid)
    }
}
