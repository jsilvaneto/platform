package com.platform.app.domain.usecase

import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.CardUsage
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.model.MonthSummary
import com.platform.app.domain.model.calculateOpenCents
import com.platform.app.domain.model.calculatePaidCents
import com.platform.app.domain.model.calculateTotalCents
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthSummaryParityTest {

    private val repository: FinancialRepository = mockk(relaxed = true)
    private val getMonthSummaryUseCase = GetMonthSummaryUseCase(repository)
    private val getOpenAmountByContactUseCase = GetOpenAmountByContactUseCase(repository)

    private fun createInstallment(
        id: String,
        amountCents: Long,
        isPaid: Boolean = false,
        dueDate: Long = 1000L,
        isPaused: Boolean = false,
        contactId: String? = null
    ): BillInstallment {
        return BillInstallment(
            id = id,
            billId = "bill-$id",
            billTitle = "Conta $id",
            amountCents = amountCents,
            dueDate = dueDate,
            installmentNumber = 1,
            totalInstallments = 1,
            paidAt = if (isPaid) dueDate else null,
            actualPaymentDate = if (isPaid) dueDate else null,
            status = when {
                isPaused -> BillStatus.PAUSED
                isPaid -> BillStatus.PAID
                else -> BillStatus.PENDING
            },
            type = BillType.SINGLE,
            contactId = contactId,
            contactName = if (contactId != null) "Contato $contactId" else null,
            categoryName = "Geral",
            categoryColorHex = "#000000",
            categoryIconName = "category",
            nature = ExpenseNature.NECESSARIO
        )
    }

    @Test
    fun `MonthSummary calculation excludes paused installments and correctly categorizes statuses`() {
        val now = 1700000000000L
        val installments = listOf(
            createInstallment("1", 10000L, isPaid = true, dueDate = now - 200000000L), // Pago: 100
            createInstallment("2", 5000L, isPaid = false, dueDate = now - 100000000L), // Vencido: 50
            createInstallment("3", 7500L, isPaid = false, dueDate = now + 100000000L), // Pendente: 75
            createInstallment("4", 20000L, isPaid = false, dueDate = now + 100000000L, isPaused = true) // Pausado: Ignorado
        )

        val summary = getMonthSummaryUseCase.calculate(installments, nowMillis = now)

        assertEquals(22500L, summary.totalCents) // 10000 + 5000 + 7500
        assertEquals(10000L, summary.paidCents)
        assertEquals(5000L, summary.overdueCents)
        assertEquals(7500L, summary.pendingCents)
        assertEquals(1, summary.paidCount)
        assertEquals(1, summary.overdueCount)
        assertEquals(1, summary.pendingCount)
        assertEquals(3, summary.totalCount)

        // Verificação de paridade com as extensões de domínio
        assertEquals(22500L, installments.calculateTotalCents())
        assertEquals(10000L, installments.calculatePaidCents())
        assertEquals(12500L, installments.calculateOpenCents()) // 5000 + 7500
    }

    @Test
    fun `GetOpenAmountByContactUseCase calculation groups open non-paused balance correctly`() {
        val installments = listOf(
            createInstallment("1", 5000L, isPaid = false, contactId = "contact-A"),
            createInstallment("2", 3000L, isPaid = false, contactId = "contact-A"),
            createInstallment("3", 4000L, isPaid = true, contactId = "contact-A"), // Pago -> ignorado
            createInstallment("4", 10000L, isPaid = false, contactId = "contact-A", isPaused = true), // Pausado -> ignorado
            createInstallment("5", 8000L, isPaid = false, contactId = "contact-B")
        )

        val openBalances = getOpenAmountByContactUseCase.calculate(installments)

        assertEquals(8000L, openBalances["contact-A"])
        assertEquals(8000L, openBalances["contact-B"])
        assertEquals(2, openBalances.size)
    }

    @Test
    fun `CardUsage calculation handles available limit and limit exceed flag`() {
        val card = CreditCard(
            id = "card-1",
            name = "Nubank",
            totalLimitCents = 500000L, // R$ 5.000
            closingDay = 1,
            dueDay = 10,
            colorHex = "#820AD1"
        )

        val invoice = CreditCardInvoice(
            id = "inv-1",
            creditCardId = "card-1",
            referenceMonth = "2026-10",
            closingDate = 1000L,
            dueDate = 2000L,
            status = InvoiceStatus.ABERTA,
            totalAmountCents = 150000L
        )

        val summaryNormal = CreditCardWithInvoiceSummary(
            card = card,
            currentInvoice = invoice,
            usedLimitCents = 200000L // R$ 2.000 usado
        )

        val usageNormal = CardUsage.fromSummary(summaryNormal)
        assertEquals(500000L, usageNormal.totalLimitCents)
        assertEquals(200000L, usageNormal.usedLimitCents)
        assertEquals(300000L, usageNormal.availableLimitCents)
        assertEquals(0.4f, usageNormal.usagePercentage, 0.001f)
        assertEquals(150000L, usageNormal.currentInvoiceAmountCents)
        assertFalse(usageNormal.isLimitExceeded)

        // Cenário de limite estourado
        val summaryExceeded = CreditCardWithInvoiceSummary(
            card = card,
            currentInvoice = invoice,
            usedLimitCents = 600000L // R$ 6.000 usado de 5.000
        )
        val usageExceeded = CardUsage.fromSummary(summaryExceeded)
        assertEquals(0L, usageExceeded.availableLimitCents)
        assertEquals(1.0f, usageExceeded.usagePercentage, 0.001f)
        assertTrue(usageExceeded.isLimitExceeded)
    }
}
