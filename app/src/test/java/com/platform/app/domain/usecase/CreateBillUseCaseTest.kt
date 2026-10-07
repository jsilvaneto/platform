package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class CreateBillUseCaseTest {

    private lateinit var repository: FinancialRepository
    private lateinit var calculateInstallmentsUseCase: CalculateInstallmentsUseCase
    private lateinit var createBillUseCase: CreateBillUseCase

    private val sampleCard = CreditCard(
        id = "card-1",
        name = "Nubank",
        totalLimitCents = 500000L,
        closingDay = 25,
        dueDay = 5
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        calculateInstallmentsUseCase = CalculateInstallmentsUseCase()
        createBillUseCase = CreateBillUseCase(
            repository = repository,
            calculateInstallmentsUseCase = calculateInstallmentsUseCase
        )

        // Mock para faturas por mês
        coEvery { repository.getOrCreateInvoiceForMonth(sampleCard.id, any()) } answers {
            val cardId = firstArg<String>()
            val refMonth = secondArg<String>()
            CreditCardInvoice(
                id = "inv-$refMonth",
                creditCardId = cardId,
                referenceMonth = refMonth,
                closingDate = 1729897199000L,
                dueDate = 1730761199000L,
                status = InvoiceStatus.ABERTA
            )
        }
    }

    @Test
    fun `installment bill must have absolute parity between credit card and non-card flows in values, remainder and count`() = runTest {
        // Despesa de R$ 100,00 dividida em 3 parcelas (10000 centavos / 3 = 3333 + 1 resto)
        val firstDueDate = 1727395200000L // 27/09/2026 (após dia de corte 25)

        val billWithoutCard = Bill(
            id = "bill-without-card",
            title = "Compra Notebook",
            type = BillType.INSTALLMENT,
            totalAmountCents = 10000L,
            totalInstallments = 3
        )

        val billWithCard = Bill(
            id = "bill-with-card",
            title = "Compra Notebook",
            type = BillType.INSTALLMENT,
            totalAmountCents = 10000L,
            totalInstallments = 3
        )

        // 1. Executa fluxo SEM cartão
        val installmentsWithoutCard = createBillUseCase(
            bill = billWithoutCard,
            firstDueDate = firstDueDate,
            creditCard = null
        )

        // 2. Executa fluxo COM cartão
        val installmentsWithCard = createBillUseCase(
            bill = billWithCard,
            firstDueDate = firstDueDate,
            creditCard = sampleCard
        )

        // PARIDADE DE CONTAGEM
        assertEquals(3, installmentsWithoutCard.size)
        assertEquals(3, installmentsWithCard.size)
        assertEquals(installmentsWithoutCard.size, installmentsWithCard.size)

        // PARIDADE DE VALORES E RESTO NA 1ª PARCELA
        assertEquals(3334L, installmentsWithoutCard[0].amountCents)
        assertEquals(3334L, installmentsWithCard[0].amountCents)

        assertEquals(3333L, installmentsWithoutCard[1].amountCents)
        assertEquals(3333L, installmentsWithCard[1].amountCents)

        assertEquals(3333L, installmentsWithoutCard[2].amountCents)
        assertEquals(3333L, installmentsWithCard[2].amountCents)

        // Paridade individual por parcela
        for (i in 0 until 3) {
            assertEquals(
                "Valor da parcela $i deve ser rigorosamente igual nos dois fluxos",
                installmentsWithoutCard[i].amountCents,
                installmentsWithCard[i].amountCents
            )
            assertEquals(
                "Número da parcela $i deve coincidir",
                installmentsWithoutCard[i].installmentNumber,
                installmentsWithCard[i].installmentNumber
            )
            assertEquals(
                "Total de parcelas deve coincidir",
                installmentsWithoutCard[i].totalInstallments,
                installmentsWithCard[i].totalInstallments
            )
        }

        // Soma total rigorosa de 10000 centavos em ambos
        assertEquals(10000L, installmentsWithoutCard.sumOf { it.amountCents })
        assertEquals(10000L, installmentsWithCard.sumOf { it.amountCents })

        // No fluxo sem cartão: invoiceId deve ser nulo
        installmentsWithoutCard.forEach { assertNull(it.invoiceId) }

        // No fluxo com cartão: invoiceId deve estar associado à fatura correspondente
        installmentsWithCard.forEach { assertNotNull(it.invoiceId) }

        // Verifica que as datas-base usadas para mapear as faturas no cartão foram exatamente as do parcelamento
        val expectedBaseDate1 = firstDueDate
        val expectedBaseDate2 = DateUtils.addMonths(firstDueDate, 1)
        val expectedBaseDate3 = DateUtils.addMonths(firstDueDate, 2)

        val refMonth1 = CreditCardCalculator.determineInvoiceReferenceMonth(expectedBaseDate1, sampleCard.closingDay)
        val refMonth2 = CreditCardCalculator.determineInvoiceReferenceMonth(expectedBaseDate2, sampleCard.closingDay)
        val refMonth3 = CreditCardCalculator.determineInvoiceReferenceMonth(expectedBaseDate3, sampleCard.closingDay)

        coVerify(exactly = 1) { repository.getOrCreateInvoiceForMonth(sampleCard.id, refMonth1) }
        coVerify(exactly = 1) { repository.getOrCreateInvoiceForMonth(sampleCard.id, refMonth2) }
        coVerify(exactly = 1) { repository.getOrCreateInvoiceForMonth(sampleCard.id, refMonth3) }
    }

    @Test
    fun `single bill parity between card and non-card flow`() = runTest {
        val dueDate = 1727395200000L
        val amount = 15990L

        val billNoCard = Bill(
            id = "bill-s-1",
            title = "Supermercado",
            type = BillType.SINGLE,
            totalAmountCents = amount
        )

        val billCard = Bill(
            id = "bill-s-2",
            title = "Supermercado",
            type = BillType.SINGLE,
            totalAmountCents = amount
        )

        val instNoCard = createBillUseCase(billNoCard, dueDate, creditCard = null)
        val instCard = createBillUseCase(billCard, dueDate, creditCard = sampleCard)

        assertEquals(1, instNoCard.size)
        assertEquals(1, instCard.size)
        assertEquals(amount, instNoCard[0].amountCents)
        assertEquals(amount, instCard[0].amountCents)

        assertNull(instNoCard[0].invoiceId)
        assertNotNull(instCard[0].invoiceId)
    }

    @Test
    fun `recurring bill parity in occurrences and amounts`() = runTest {
        val dueDate = 1727395200000L
        val amount = 4990L // R$ 49,90 por mês

        val billNoCard = Bill(
            id = "rec-no-card",
            title = "Academia",
            type = BillType.RECURRING,
            totalAmountCents = amount,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER
        )

        val billCard = Bill(
            id = "rec-card",
            title = "Academia",
            type = BillType.RECURRING,
            totalAmountCents = amount,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER
        )

        val instNoCard = createBillUseCase(billNoCard, dueDate, creditCard = null)
        val instCard = createBillUseCase(billCard, dueDate, creditCard = sampleCard)

        assertEquals(12, instNoCard.size)
        assertEquals(12, instCard.size)

        for (i in 0 until 12) {
            assertEquals(amount, instNoCard[i].amountCents)
            assertEquals(amount, instCard[i].amountCents)
            assertNull(instNoCard[i].invoiceId)
        }
        // Primeira ocorrência vinculada à fatura do ciclo atual; ocorrências futuras criadas sob demanda
        assertNotNull(instCard[0].invoiceId)
        for (i in 1 until 12) {
            assertNull(instCard[i].invoiceId)
        }
    }

    @Test
    fun `isFirstInstallmentPaid correctly marks first installment as PAID and remaining as PENDING in both flows`() = runTest {
        val dueDate = 1727395200000L
        val paymentTime = 1727399999000L

        val bill = Bill(
            id = "bill-paid-test",
            title = "Manutenção",
            type = BillType.INSTALLMENT,
            totalAmountCents = 6000L,
            totalInstallments = 2
        )

        val instNoCard = createBillUseCase(
            bill = bill,
            firstDueDate = dueDate,
            creditCard = null,
            isFirstInstallmentPaid = true,
            actualPaymentDate = paymentTime
        )

        val instCard = createBillUseCase(
            bill = bill,
            firstDueDate = dueDate,
            creditCard = sampleCard,
            isFirstInstallmentPaid = true,
            actualPaymentDate = paymentTime
        )

        // Fluxo sem cartão
        assertEquals(BillStatus.PAID, instNoCard[0].status)
        assertEquals(paymentTime, instNoCard[0].paidAt)
        assertEquals(paymentTime, instNoCard[0].actualPaymentDate)
        assertEquals(BillStatus.PENDING, instNoCard[1].status)
        assertNull(instNoCard[1].paidAt)

        // Fluxo com cartão
        assertEquals(BillStatus.PAID, instCard[0].status)
        assertEquals(paymentTime, instCard[0].paidAt)
        assertEquals(paymentTime, instCard[0].actualPaymentDate)
        assertEquals(BillStatus.PENDING, instCard[1].status)
        assertNull(instCard[1].paidAt)
    }

    @Test
    fun `bill entity is saved with first invoiceId when credit card is present`() = runTest {
        val billSlot = slot<Bill>()
        coEvery { repository.saveBillWithInstallments(capture(billSlot), any()) } returns Unit

        val bill = Bill(
            id = "bill-card-save",
            title = "Restaurante",
            type = BillType.SINGLE,
            totalAmountCents = 8500L
        )

        val timestamp = 1727395200000L
        createBillUseCase(bill, timestamp, creditCard = sampleCard)

        val expectedRefMonth = CreditCardCalculator.determineInvoiceReferenceMonth(timestamp, sampleCard.closingDay)
        assertEquals("inv-$expectedRefMonth", billSlot.captured.invoiceId)
    }
}
