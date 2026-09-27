package com.platform.app.domain.usecase

import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillType
import org.junit.Assert.assertEquals
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
    fun `recurring bill should project 12 monthly installments with fixed amount`() {
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
}
