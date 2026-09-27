package com.platform.app.domain.usecase

import app.cash.turbine.test
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.Contact
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetContactDetailsUseCaseTest {

    private lateinit var repository: FinancialRepository
    private lateinit var useCase: GetContactDetailsUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = GetContactDetailsUseCase(repository)
    }

    @Test
    fun `when contact exists, should return aggregated details with bills and planned installments`() = runTest {
        val contact = Contact(
            id = "c1",
            name = "Imobiliária Central",
            phone = "11999999999",
            email = "contato@imob.com",
            city = "São Paulo",
            state = "SP"
        )
        val bill = Bill(
            id = "b1",
            title = "Aluguel",
            type = BillType.RECURRING,
            totalAmountCents = 200000L,
            contactId = "c1"
        )
        val pendingInstallment = BillInstallment(
            id = "i1",
            billId = "b1",
            billTitle = "Aluguel",
            amountCents = 200000L,
            dueDate = System.currentTimeMillis() + 100000,
            paidAt = null
        )
        val paidInstallment = BillInstallment(
            id = "i0",
            billId = "b1",
            billTitle = "Aluguel",
            amountCents = 200000L,
            dueDate = System.currentTimeMillis() - 100000,
            paidAt = System.currentTimeMillis() - 90000
        )

        every { repository.getContactById("c1") } returns flowOf(contact)
        every { repository.getBillsByContact("c1") } returns flowOf(listOf(bill))
        every { repository.getPlannedInstallmentsByContact("c1") } returns flowOf(listOf(pendingInstallment))
        every { repository.getInstallmentsByContact("c1") } returns flowOf(listOf(pendingInstallment, paidInstallment))

        useCase("c1").test {
            val details = awaitItem()
            assertNotNull(details)
            assertEquals("Imobiliária Central", details!!.contact.name)
            assertEquals(1, details.bills.size)
            assertEquals(1, details.plannedInstallments.size)
            assertEquals(200000L, details.totalPendingAmountCents)
            assertEquals(200000L, details.totalPaidAmountCents)
            awaitComplete()
        }
    }

    @Test
    fun `when contact does not exist, should emit null`() = runTest {
        every { repository.getContactById("not-found") } returns flowOf(null)
        every { repository.getBillsByContact("not-found") } returns flowOf(emptyList())
        every { repository.getPlannedInstallmentsByContact("not-found") } returns flowOf(emptyList())
        every { repository.getInstallmentsByContact("not-found") } returns flowOf(emptyList())

        useCase("not-found").test {
            val details = awaitItem()
            assertNull(details)
            awaitComplete()
        }
    }
}
