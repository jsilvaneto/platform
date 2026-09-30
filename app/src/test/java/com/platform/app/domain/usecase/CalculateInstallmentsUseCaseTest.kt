package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import org.junit.Assert.assertEquals
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
}
