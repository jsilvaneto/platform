package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateInstallmentsUseCaseTest {

    private lateinit var useCase: CalculateInstallmentsUseCase

    @Before
    fun setUp() {
        useCase = CalculateInstallmentsUseCase()
    }

    @Test
    fun `single bill should generate exactly 1 installment with total amount`() {
        val bill = Bill(
            id = "1",
            title = "Conta de Luz",
            type = BillType.SINGLE,
            totalAmountCents = 15050L // R$ 150,50
        )
        val installments = useCase(bill, 1727395200000L)

        assertEquals(1, installments.size)
        assertEquals(15050L, installments[0].amountCents)
        assertEquals(1, installments[0].installmentNumber)
        assertEquals(1, installments[0].totalInstallments)
        assertEquals(BillType.SINGLE, installments[0].type)
    }

    @Test
    fun `installment bill should divide cents exactly with remainder on first installment`() {
        // R$ 100,00 dividido em 3x (10000 centavos / 3 = 3333 + 1 resto)
        val bill = Bill(
            id = "2",
            title = "Compra Notebook",
            type = BillType.INSTALLMENT,
            totalAmountCents = 10000L,
            totalInstallments = 3
        )
        val installments = useCase(bill, 1727395200000L)

        assertEquals(3, installments.size)
        assertEquals(3334L, installments[0].amountCents) // primeira com resto
        assertEquals(3333L, installments[1].amountCents)
        assertEquals(3333L, installments[2].amountCents)

        // Soma total das parcelas deve ser rigorosamente 10000 centavos
        val sum = installments.sumOf { it.amountCents }
        assertEquals(10000L, sum)
    }

    @Test
    fun `recurring bill should project 12 monthly installments with fixed amount by default`() {
        val bill = Bill(
            id = "3",
            title = "Internet Fibra",
            type = BillType.RECURRING,
            totalAmountCents = 9990L // R$ 99,90
        )
        val installments = useCase(bill, 1727395200000L)

        assertEquals(12, installments.size)
        installments.forEach { inst ->
            assertEquals(9990L, inst.amountCents)
            assertEquals(BillType.RECURRING, inst.type)
        }
    }

    @Test
    fun `recurring bill with DAILY frequency and BY_OCCURRENCES should generate exact daily installments`() {
        val startDate = 1727395200000L
        val bill = Bill(
            id = "4",
            title = "Almoço Diário",
            type = BillType.RECURRING,
            totalAmountCents = 2500L,
            totalInstallments = 7,
            recurrenceFrequency = RecurrenceFrequency.DAILY,
            recurrenceEndType = RecurrenceEndType.BY_OCCURRENCES
        )
        val installments = useCase(bill, startDate)

        assertEquals(7, installments.size)
        for (i in 0 until 7) {
            val expectedDate = DateUtils.addDays(startDate, i)
            assertEquals(expectedDate, installments[i].dueDate)
            assertEquals(i + 1, installments[i].installmentNumber)
            assertEquals(7, installments[i].totalInstallments)
            assertEquals(2500L, installments[i].amountCents)
        }
    }

    @Test
    fun `recurring bill with WEEKLY frequency and UNTIL_DATE should generate installments up to end date`() {
        val startDate = 1727395200000L
        // 3 semanas depois
        val endDate = DateUtils.addWeeks(startDate, 3)

        val bill = Bill(
            id = "5",
            title = "Aula Particular Semanal",
            type = BillType.RECURRING,
            totalAmountCents = 8000L,
            recurrenceFrequency = RecurrenceFrequency.WEEKLY,
            recurrenceEndType = RecurrenceEndType.UNTIL_DATE,
            recurrenceEndDate = endDate
        )
        val installments = useCase(bill, startDate)

        // Semana 0, Semana 1, Semana 2, Semana 3 = 4 ocorrências
        assertEquals(4, installments.size)
        assertEquals(startDate, installments[0].dueDate)
        assertEquals(DateUtils.addWeeks(startDate, 1), installments[1].dueDate)
        assertEquals(DateUtils.addWeeks(startDate, 2), installments[2].dueDate)
        assertEquals(DateUtils.addWeeks(startDate, 3), installments[3].dueDate)
        installments.forEach {
            assertTrue(it.dueDate <= DateUtils.getEndOfDay(endDate))
            assertEquals(4, it.totalInstallments)
        }
    }

    @Test
    fun `recurring bill with YEARLY frequency and FOREVER should project 5 yearly installments`() {
        val startDate = 1727395200000L
        val bill = Bill(
            id = "6",
            title = "IPVA Anual",
            type = BillType.RECURRING,
            totalAmountCents = 150000L,
            recurrenceFrequency = RecurrenceFrequency.YEARLY,
            recurrenceEndType = RecurrenceEndType.FOREVER
        )
        val installments = useCase(bill, startDate)

        assertEquals(5, installments.size)
        for (i in 0 until 5) {
            assertEquals(DateUtils.addYears(startDate, i), installments[i].dueDate)
            assertEquals(i + 1, installments[i].installmentNumber)
            assertEquals(5, installments[i].totalInstallments)
        }
    }

    @Test
    fun `generateNextRecurringInstallments should extend monthly FOREVER bill with installments 13 to 24`() {
        val startDate = 1727395200000L // 27/09/2024
        val bill = Bill(
            id = "bill-forever-1",
            title = "Academia Smart Fit",
            type = BillType.RECURRING,
            totalAmountCents = 11990L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER
        )
        val initialInstallments = useCase(bill, startDate)
        assertEquals(12, initialInstallments.size)

        // Estender com a próxima janela
        val nextInstallments = useCase.generateNextRecurringInstallments(bill, initialInstallments)

        assertEquals(12, nextInstallments.size)
        // Números de parcela devem ser contínuos: 13 a 24
        assertEquals(13, nextInstallments.first().installmentNumber)
        assertEquals(24, nextInstallments.last().installmentNumber)
        assertEquals(24, nextInstallments.first().totalInstallments)
        assertEquals(24, nextInstallments.last().totalInstallments)

        // Datas devem ser passos 12..23 em relação à startDate
        for (i in 0 until 12) {
            val expectedDueDate = DateUtils.addMonths(startDate, 12 + i)
            assertEquals(expectedDueDate, nextInstallments[i].dueDate)
            assertEquals(11990L, nextInstallments[i].amountCents)
            assertEquals(BillType.RECURRING, nextInstallments[i].type)
        }
    }

    @Test
    fun `generateNextRecurringInstallments should preserve step calculation even if earlier installments were deleted`() {
        val startDate = 1727395200000L
        val bill = Bill(
            id = "bill-forever-2",
            title = "Streaming",
            type = BillType.RECURRING,
            totalAmountCents = 3990L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER
        )
        val initialInstallments = useCase(bill, startDate)
        // Suponha que as parcelas 1 a 6 foram excluídas/liquidadas e restam apenas 7 a 12
        val remainingInstallments = initialInstallments.filter { it.installmentNumber >= 7 }
        assertEquals(6, remainingInstallments.size)

        val nextInstallments = useCase.generateNextRecurringInstallments(bill, remainingInstallments)

        assertEquals(12, nextInstallments.size)
        assertEquals(13, nextInstallments.first().installmentNumber)
        assertEquals(24, nextInstallments.last().installmentNumber)

        // A data da parcela 13 deve ser exatamente 12 meses após startDate (ou 6 meses após a parcela 7)
        val expectedFirstNewDueDate = DateUtils.addMonths(startDate, 12)
        assertEquals(expectedFirstNewDueDate, nextInstallments.first().dueDate)
    }

    @Test
    fun `generateNextRecurringInstallments should return empty list if bill is not RECURRING or existing is empty`() {
        val singleBill = Bill(id = "s1", title = "Luz", type = BillType.SINGLE, totalAmountCents = 1000L)
        val initial = useCase(singleBill, 1727395200000L)

        val nextForSingle = useCase.generateNextRecurringInstallments(singleBill, initial)
        assertTrue(nextForSingle.isEmpty())

        val recurringBill = Bill(id = "r1", title = "Assinatura", type = BillType.RECURRING, totalAmountCents = 1000L)
        val nextForEmpty = useCase.generateNextRecurringInstallments(recurringBill, emptyList())
        assertTrue(nextForEmpty.isEmpty())
    }

    @Test
    fun `generateNextRecurringInstallments should not copy bill invoiceId and set invoiceId to null`() {
        val startDate = 1727395200000L
        val bill = Bill(
            id = "bill-forever-inv",
            title = "Assinatura Streaming",
            type = BillType.RECURRING,
            totalAmountCents = 4590L,
            invoiceId = "invoice-oct-2026", // 1ª fatura vinculada na conta
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER,
            recurrenceAnchorDate = startDate
        )
        val initialInstallments = useCase(bill, startDate).mapIndexed { index, inst ->
            if (index == 0) inst.copy(invoiceId = "invoice-oct-2026") else inst.copy(invoiceId = null)
        }

        val nextInstallments = useCase.generateNextRecurringInstallments(bill, initialInstallments, 6)

        assertEquals(6, nextInstallments.size)
        // Nenhuma parcela estendida deve herdar invoice-oct-2026
        nextInstallments.forEach {
            assertNull("Parcelas estendidas devem ter invoiceId nulo", it.invoiceId)
        }
    }

    @Test
    fun `generateNextRecurringInstallments should anchor to recurrenceAnchorDate when firstInstallment dueDate was modified by invoice`() {
        val anchorDate = 1728518400000L // 10/10/2026 (dia 10)
        val invoiceDueDate = 1729987200000L // 27/10/2026 (dia 27, vencimento da fatura do cartão)

        val bill = Bill(
            id = "bill-card-rec",
            title = "GymPass",
            type = BillType.RECURRING,
            totalAmountCents = 8990L,
            invoiceId = "inv-1",
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER,
            recurrenceAnchorDate = anchorDate
        )

        // Simula 12 parcelas onde a 1ª teve dueDate alterada para o vencimento da fatura (dia 27)
        val initialInstallments = useCase(bill, anchorDate).mapIndexed { index, inst ->
            if (index == 0) inst.copy(dueDate = invoiceDueDate, invoiceId = "inv-1")
            else inst.copy(invoiceId = null)
        }

        // Gera parcelas 13 e 14
        val nextInstallments = useCase.generateNextRecurringInstallments(bill, initialInstallments, 2)

        assertEquals(2, nextInstallments.size)
        assertEquals(13, nextInstallments[0].installmentNumber)
        assertEquals(14, nextInstallments[1].installmentNumber)

        // Parcela 13 deve ser exatamente 12 meses após anchorDate (10/10/2027), NÃO ancorada em 27/10/2026
        val expectedDate13 = DateUtils.addMonths(anchorDate, 12)
        val expectedDate14 = DateUtils.addMonths(anchorDate, 13)

        assertEquals(expectedDate13, nextInstallments[0].dueDate)
        assertEquals(expectedDate14, nextInstallments[1].dueDate)
        assertNull(nextInstallments[0].invoiceId)
        assertNull(nextInstallments[1].invoiceId)
    }
}
