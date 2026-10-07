package com.platform.app.domain.usecase

import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.UUID

class GetCreditCardSummariesUseCaseTest {

    private lateinit var repository: FinancialRepository
    private lateinit var useCase: GetCreditCardSummariesUseCase

    private val card = CreditCard(
        id = "card-nubank",
        name = "Nubank",
        totalLimitCents = 500000L, // R$ 5.000,00
        closingDay = 25,
        dueDay = 5
    )

    // Data de referência: 15/10/2026 (fatura atual: 2026-10)
    private val currentTimestamp: Long = run {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.OCTOBER, 15, 12, 0, 0)
        cal.timeInMillis
    }

    private val invoiceOct = CreditCardInvoice(
        id = "inv-2026-10",
        creditCardId = card.id,
        referenceMonth = "2026-10",
        closingDate = 1729897199000L,
        dueDate = 1730761199000L,
        status = InvoiceStatus.ABERTA
    )

    private val invoiceNov = CreditCardInvoice(
        id = "inv-2026-11",
        creditCardId = card.id,
        referenceMonth = "2026-11",
        closingDate = 1732575599000L,
        dueDate = 1733439599000L,
        status = InvoiceStatus.ABERTA
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = GetCreditCardSummariesUseCase(repository)
    }

    @Test
    fun `card with 1 installment 10x, 1 subscription and 1 single purchase should calculate used and available limit accurately`() {
        // 1. Compra parcelada em 10x de R$ 100,00 (total R$ 1.000,00 = 100.000 centavos)
        // Parcela 1 na fatura atual (2026-10), parcelas 2..10 em faturas futuras
        val installmentItems = (1..10).map { i ->
            val invId = if (i == 1) invoiceOct.id else "inv-future-$i"
            BillInstallment(
                id = "inst-$i",
                billId = "bill-installment-10x",
                billTitle = "Smartphone",
                installmentNumber = i,
                totalInstallments = 10,
                amountCents = 10000L, // R$ 100,00
                dueDate = 1727395200000L + (i * 30L * 86400000L),
                invoiceId = invId,
                status = BillStatus.PENDING,
                type = BillType.INSTALLMENT
            )
        }

        // 2. Compra à vista de R$ 200,00 (20.000 centavos) na fatura atual
        val singleItem = BillInstallment(
            id = "single-1",
            billId = "bill-single",
            billTitle = "Supermercado",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 20000L, // R$ 200,00
            dueDate = invoiceOct.dueDate,
            invoiceId = invoiceOct.id,
            status = BillStatus.PENDING,
            type = BillType.SINGLE
        )

        // 3. Assinatura recorrente de R$ 50,00/mês (5.000 centavos)
        // Ocorrência 1 na fatura atual (2026-10)
        val recurringCurrentItem = BillInstallment(
            id = "rec-1",
            billId = "bill-sub",
            billTitle = "Netflix",
            installmentNumber = 1,
            totalInstallments = 12,
            amountCents = 5000L, // R$ 50,00
            dueDate = invoiceOct.dueDate,
            invoiceId = invoiceOct.id,
            status = BillStatus.PENDING,
            type = BillType.RECURRING
        )

        // Ocorrências futuras 2..12 da assinatura (meses futuros)
        // Mesmo se estiverem em faturas futuras ou sem fatura, NÃO devem consumir o limite do cartão
        val recurringFutureItems = (2..12).map { i ->
            BillInstallment(
                id = "rec-$i",
                billId = "bill-sub",
                billTitle = "Netflix",
                installmentNumber = i,
                totalInstallments = 12,
                amountCents = 5000L, // R$ 50,00
                dueDate = 1727395200000L + (i * 30L * 86400000L),
                invoiceId = "inv-future-rec-$i",
                status = BillStatus.PENDING,
                type = BillType.RECURRING
            )
        }

        val futureInvoices = (2..10).map { i ->
            CreditCardInvoice(
                id = "inv-future-$i",
                creditCardId = card.id,
                referenceMonth = String.format(java.util.Locale.US, "2027-%02d", i),
                closingDate = 1732575599000L,
                dueDate = 1733439599000L,
                status = InvoiceStatus.ABERTA
            )
        }

        val allInvoices = listOf(invoiceOct) + futureInvoices
        val allInstallments = installmentItems + listOf(singleItem, recurringCurrentItem) + recurringFutureItems

        val summaries = useCase.calculateSummaries(
            cards = listOf(card),
            allInvoices = allInvoices,
            allInstallments = allInstallments,
            currentTimestamp = currentTimestamp
        )

        assertEquals(1, summaries.size)
        val summary = summaries[0]

        assertEquals(card.id, summary.card.id)
        assertNotNull(summary.currentInvoice)
        assertEquals(invoiceOct.id, summary.currentInvoice?.id)

        // Regra Única:
        // INSTALLMENT: consome saldo devedor restante de todas as 10 parcelas = 100.000 centavos (R$ 1.000,00)
        // SINGLE: consome o que está na fatura atual = 20.000 centavos (R$ 200,00)
        // RECURRING: consome apenas a ocorrência da fatura atual = 5.000 centavos (R$ 50,00), ignorando as outras 11!
        // Limite usado total = 100.000 + 20.000 + 5.000 = 125.000 centavos (R$ 1.250,00)
        assertEquals(125000L, summary.usedLimitCents)

        // Limite disponível: 500.000 - 125.000 = 375.000 centavos (R$ 3.750,00)
        assertEquals(375000L, summary.availableLimitCents)
    }

    @Test
    fun `when current invoice is paid, installment updates remaining debt and single and recurring of that invoice are cleared`() {
        // Fatura de Outubro QUITADA (PAGA)
        val paidInvoiceOct = invoiceOct.copy(status = InvoiceStatus.PAGA)

        // Parcela 1 paga; parcelas 2..10 ainda pendentes
        val installmentItems = (1..10).map { i ->
            val isPaid = i == 1
            BillInstallment(
                id = "inst-$i",
                billId = "bill-installment-10x",
                billTitle = "Smartphone",
                installmentNumber = i,
                totalInstallments = 10,
                amountCents = 10000L,
                dueDate = 1727395200000L + (i * 30L * 86400000L),
                invoiceId = if (i == 1) paidInvoiceOct.id else "inv-future-$i",
                paidAt = if (isPaid) currentTimestamp else null,
                actualPaymentDate = if (isPaid) currentTimestamp else null,
                status = if (isPaid) BillStatus.PAID else BillStatus.PENDING,
                type = BillType.INSTALLMENT
            )
        }

        // Compra à vista QUITADA na fatura paga
        val paidSingle = BillInstallment(
            id = "single-1",
            billId = "bill-single",
            billTitle = "Supermercado",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 20000L,
            dueDate = paidInvoiceOct.dueDate,
            invoiceId = paidInvoiceOct.id,
            paidAt = currentTimestamp,
            actualPaymentDate = currentTimestamp,
            status = BillStatus.PAID,
            type = BillType.SINGLE
        )

        // Assinatura de Outubro QUITADA na fatura paga
        val paidRecurring = BillInstallment(
            id = "rec-1",
            billId = "bill-sub",
            billTitle = "Netflix",
            installmentNumber = 1,
            totalInstallments = 12,
            amountCents = 5000L,
            dueDate = paidInvoiceOct.dueDate,
            invoiceId = paidInvoiceOct.id,
            paidAt = currentTimestamp,
            actualPaymentDate = currentTimestamp,
            status = BillStatus.PAID,
            type = BillType.RECURRING
        )

        val futureInvoices = (2..10).map { i ->
            CreditCardInvoice(
                id = "inv-future-$i",
                creditCardId = card.id,
                referenceMonth = String.format(java.util.Locale.US, "2027-%02d", i),
                closingDate = 1732575599000L,
                dueDate = 1733439599000L,
                status = InvoiceStatus.ABERTA
            )
        }

        val allInvoices = listOf(paidInvoiceOct) + futureInvoices
        val allInstallments = installmentItems + listOf(paidSingle, paidRecurring)

        val summaries = useCase.calculateSummaries(
            cards = listOf(card),
            allInvoices = allInvoices,
            allInstallments = allInstallments,
            currentTimestamp = currentTimestamp
        )

        val summary = summaries[0]

        // Agora restam 9 parcelas de R$ 100 pendentes = 90.000 centavos
        // Compra à vista e assinatura de Outubro já estão pagas = 0 centavos
        // Limite usado total = 90.000 centavos (R$ 900,00)
        assertEquals(90000L, summary.usedLimitCents)

        // Limite disponível: 500.000 - 90.000 = 410.000 centavos (R$ 4.100,00)
        assertEquals(410000L, summary.availableLimitCents)
    }

    @Test
    fun `use case flow reactive invocation combines repository flows correctly`() = runTest {
        every { repository.getCreditCards() } returns flowOf(listOf(card))
        every { repository.getAllCreditCardInvoices() } returns flowOf(listOf(invoiceOct))
        every { repository.getAllInstallments() } returns flowOf(emptyList())

        val result = useCase(currentTimestamp).first()

        assertEquals(1, result.size)
        assertEquals(card.id, result[0].card.id)
        assertEquals(0L, result[0].usedLimitCents)
        assertEquals(card.totalLimitCents, result[0].availableLimitCents)
    }
}
