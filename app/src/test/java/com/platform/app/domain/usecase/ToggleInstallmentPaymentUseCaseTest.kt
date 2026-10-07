package com.platform.app.domain.usecase

import com.platform.app.domain.repository.FinancialRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToggleInstallmentPaymentUseCaseTest {

    private lateinit var repository: FinancialRepository
    private lateinit var useCase: ToggleInstallmentPaymentUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = ToggleInstallmentPaymentUseCase(repository)
    }

    @Test
    fun `when marking unpaid installment as paid with custom actualPaymentDate, passes provided actualPaymentDate`() = runTest {
        val installmentId = "inst-123"
        val customPaymentDate = 1759000000000L

        useCase(
            installmentId = installmentId,
            currentPaidStatus = false,
            actualPaymentDate = customPaymentDate
        )

        val isPaidSlot = slot<Boolean>()
        val paidTimestampSlot = slot<Long?>()
        val actualDateSlot = slot<Long?>()

        coVerify(exactly = 1) {
            repository.toggleInstallmentPayment(
                installmentId = installmentId,
                isPaid = capture(isPaidSlot),
                paidTimestamp = capture(paidTimestampSlot),
                actualPaymentDate = capture(actualDateSlot)
            )
        }

        assertTrue(isPaidSlot.captured)
        assertNotNull(paidTimestampSlot.captured)
        assertEquals(customPaymentDate, actualDateSlot.captured)
    }

    @Test
    fun `when marking unpaid installment as paid without custom date, defaults actualPaymentDate to paidTimestamp`() = runTest {
        val installmentId = "inst-456"

        useCase(
            installmentId = installmentId,
            currentPaidStatus = false,
            actualPaymentDate = null
        )

        val isPaidSlot = slot<Boolean>()
        val paidTimestampSlot = slot<Long?>()
        val actualDateSlot = slot<Long?>()

        coVerify(exactly = 1) {
            repository.toggleInstallmentPayment(
                installmentId = installmentId,
                isPaid = capture(isPaidSlot),
                paidTimestamp = capture(paidTimestampSlot),
                actualPaymentDate = capture(actualDateSlot)
            )
        }

        assertTrue(isPaidSlot.captured)
        assertNotNull(paidTimestampSlot.captured)
        assertEquals(paidTimestampSlot.captured, actualDateSlot.captured)
    }

    @Test
    fun `when unmarking paid installment, resets isPaid to false and timestamps to null`() = runTest {
        val installmentId = "inst-789"

        useCase(
            installmentId = installmentId,
            currentPaidStatus = true
        )

        coVerify(exactly = 1) {
            repository.toggleInstallmentPayment(
                installmentId = installmentId,
                isPaid = false,
                paidTimestamp = null,
                actualPaymentDate = null
            )
        }
    }
}
