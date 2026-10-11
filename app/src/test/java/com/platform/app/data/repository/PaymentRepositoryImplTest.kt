package com.platform.app.data.repository

import com.platform.app.core.dispatcher.DispatcherProvider
import com.platform.app.data.local.dao.PaymentDao
import com.platform.app.data.local.dao.SyncDao
import com.platform.app.data.local.entity.PaymentEntity
import com.platform.app.data.local.entity.SyncOutboxEntity
import com.platform.app.domain.model.Payment
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentRepositoryImplTest {

    private val paymentDao = mockk<PaymentDao>(relaxed = true)
    private val syncDao = mockk<SyncDao>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
        override val unconfined: CoroutineDispatcher = testDispatcher
    }

    private lateinit var repository: PaymentRepositoryImpl

    @Before
    fun setUp() {
        repository = PaymentRepositoryImpl(paymentDao, syncDao, dispatcherProvider)
    }

    @Test
    fun `recordPayment should insert entity and enqueue to sync outbox`() = runTest(testDispatcher) {
        val payment = Payment(
            id = "pay_1",
            billInstallmentId = "inst_1",
            amountCents = 15000L,
            paymentDate = 1700000000000L,
            interestCents = 500L,
            fineCents = 200L,
            discountCents = 100L
        )

        repository.recordPayment(payment)

        coVerify(exactly = 1) {
            paymentDao.insertPayment(match {
                it.id == "pay_1" && it.amountCents == 15000L && it.interestCents == 500L
            })
        }
        coVerify(exactly = 1) {
            syncDao.enqueue(match {
                it.entityType == "PAYMENT" && it.entityId == "pay_1" && it.operation == "INSERT"
            })
        }
    }

    @Test
    fun `softDeletePayment should soft delete in dao and enqueue DELETE to sync outbox`() = runTest(testDispatcher) {
        repository.softDeletePayment("pay_1")

        coVerify(exactly = 1) {
            paymentDao.softDeletePayment("pay_1", any())
        }
        coVerify(exactly = 1) {
            syncDao.enqueue(match {
                it.entityType == "PAYMENT" && it.entityId == "pay_1" && it.operation == "DELETE"
            })
        }
    }

    @Test
    fun `getTotalPaidForInstallmentCents should delegate to paymentDao`() = runTest(testDispatcher) {
        coEvery { paymentDao.getTotalPaidForInstallmentCents("inst_1") } returns 25000L

        val total = repository.getTotalPaidForInstallmentCents("inst_1")

        assertEquals(25000L, total)
        coVerify(exactly = 1) { paymentDao.getTotalPaidForInstallmentCents("inst_1") }
    }

    @Test
    fun `getAllPayments should map entities to domain`() = runTest(testDispatcher) {
        val entity = PaymentEntity(
            id = "p1",
            billInstallmentId = "i1",
            amountCents = 1000L,
            paymentDate = 1000L
        )
        coEvery { paymentDao.getAllPayments() } returns flowOf(listOf(entity))

        val result = repository.getAllPayments().first()

        assertEquals(1, result.size)
        assertEquals("p1", result[0].id)
        assertEquals(1000L, result[0].amountCents)
    }
}
