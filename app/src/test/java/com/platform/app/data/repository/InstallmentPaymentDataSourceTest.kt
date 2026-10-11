package com.platform.app.data.repository

import androidx.room.withTransaction
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.PaymentDao
import com.platform.app.data.local.dao.SyncDao
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.PaymentEntity
import com.platform.app.data.local.entity.SyncOutboxEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class InstallmentPaymentDataSourceTest {

    private lateinit var database: PlatformDatabase
    private lateinit var installmentDao: BillInstallmentDao
    private lateinit var paymentDao: PaymentDao
    private lateinit var syncDao: SyncDao
    private lateinit var dataSource: InstallmentPaymentDataSource

    @Before
    fun setUp() {
        database = mockk(relaxed = true)
        installmentDao = mockk(relaxed = true)
        paymentDao = mockk(relaxed = true)
        syncDao = mockk(relaxed = true)

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Any>()
        coEvery { database.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        dataSource = InstallmentPaymentDataSource(
            database = database,
            installmentDao = installmentDao,
            paymentDao = paymentDao,
            syncDao = syncDao
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    private fun sampleInstallment(
        id: String = "inst_1",
        amountCents: Long = 10000L,
        status: String = "PENDING",
        paidAt: Long? = null
    ) = BillInstallmentEntity(
        id = id,
        billId = "bill_1",
        installmentNumber = 1,
        totalInstallments = 1,
        amountCents = amountCents,
        dueDate = LocalDate.of(2026, 10, 15),
        paidAt = paidAt,
        status = status,
        itemId = null,
        invoiceId = null,
        contactId = null,
        financialAccountId = "acc_1",
        paymentMethodId = "pm_1"
    )

    @Test
    fun `toggleInstallmentPayment to true inserts Payment, updates status to PAID, and enqueues outbox`() = runTest {
        val inst = sampleInstallment()
        coEvery { installmentDao.getEntityById("inst_1") } returns inst
        coEvery { paymentDao.getPaymentsForInstallmentSync("inst_1") } returns emptyList()

        dataSource.toggleInstallmentPayment(
            installmentId = "inst_1",
            isPaid = true,
            paidTimestamp = 1728518400000L,
            actualPaymentDate = 1728518400000L
        )

        val paymentSlot = slot<PaymentEntity>()
        coVerify(exactly = 1) { paymentDao.insertPayment(capture(paymentSlot)) }
        assertEquals("inst_1", paymentSlot.captured.billInstallmentId)
        assertEquals(10000L, paymentSlot.captured.amountCents)
        assertEquals(1728518400000L, paymentSlot.captured.paymentDate)
        assertEquals("acc_1", paymentSlot.captured.financialAccountId)
        assertEquals("pm_1", paymentSlot.captured.paymentMethodId)

        coVerify(exactly = 1) {
            installmentDao.updatePayment("inst_1", 1728518400000L, 1728518400000L, "PAID", any())
        }

        coVerify(atLeast = 1) {
            syncDao.enqueue(match { it.entityType == "PAYMENT" && it.operation == "INSERT" })
            syncDao.enqueue(match { it.entityType == "BILL_INSTALLMENT" && it.operation == "UPDATE" })
        }
    }

    @Test
    fun `toggleInstallmentPayment to false soft deletes payments and resets status to PENDING`() = runTest {
        dataSource.toggleInstallmentPayment(
            installmentId = "inst_1",
            isPaid = false,
            paidTimestamp = null,
            actualPaymentDate = null
        )

        coVerify(exactly = 1) {
            paymentDao.softDeletePaymentsForInstallment("inst_1", any(), any())
        }

        coVerify(exactly = 1) {
            installmentDao.updatePayment("inst_1", null, null, "PENDING", any())
        }

        coVerify(atLeast = 1) {
            syncDao.enqueue(match { it.entityType == "PAYMENT" && it.operation == "DELETE_ALL_FOR_INSTALLMENT" })
            syncDao.enqueue(match { it.entityType == "BILL_INSTALLMENT" && it.operation == "UPDATE" })
        }
    }

    @Test
    fun `recordInstallmentPayment partial amount sets status to PARTIALLY_PAID with null paidAt`() = runTest {
        val inst = sampleInstallment(amountCents = 10000L)
        coEvery { installmentDao.getEntityById("inst_1") } returns inst

        val partialPayment = PaymentEntity(
            id = "pay_p1",
            billInstallmentId = "inst_1",
            amountCents = 4000L,
            paymentDate = 1728518400000L,
            paymentMethodId = "pm_1",
            financialAccountId = "acc_1",
            interestCents = 0L,
            fineCents = 0L,
            discountCents = 0L,
            notes = "Primeira parcela parcial",
            createdAt = 1728518400000L,
            updatedAt = 1728518400000L,
            deletedAt = null
        )
        coEvery { paymentDao.getPaymentsForInstallmentSync("inst_1") } returns listOf(partialPayment)

        dataSource.recordInstallmentPayment(
            installmentId = "inst_1",
            amountCents = 4000L,
            paymentDate = 1728518400000L
        )

        coVerify(exactly = 1) {
            installmentDao.updatePayment(
                id = "inst_1",
                paidAt = null,
                actualPaymentDate = 1728518400000L,
                status = "PARTIALLY_PAID",
                updatedAt = any()
            )
        }
    }

    @Test
    fun `recordInstallmentPayment full remaining amount sets status to PAID with non-null paidAt`() = runTest {
        val inst = sampleInstallment(amountCents = 10000L)
        coEvery { installmentDao.getEntityById("inst_1") } returns inst

        val firstPayment = PaymentEntity(
            id = "pay_p1",
            billInstallmentId = "inst_1",
            amountCents = 4000L,
            paymentDate = 1728518400000L,
            paymentMethodId = "pm_1",
            financialAccountId = "acc_1",
            interestCents = 0L,
            fineCents = 0L,
            discountCents = 0L,
            notes = null,
            createdAt = 1728518400000L,
            updatedAt = 1728518400000L,
            deletedAt = null
        )
        val secondPayment = PaymentEntity(
            id = "pay_p2",
            billInstallmentId = "inst_1",
            amountCents = 6000L,
            paymentDate = 1728604800000L,
            paymentMethodId = "pm_1",
            financialAccountId = "acc_1",
            interestCents = 0L,
            fineCents = 0L,
            discountCents = 0L,
            notes = null,
            createdAt = 1728604800000L,
            updatedAt = 1728604800000L,
            deletedAt = null
        )
        coEvery { paymentDao.getPaymentsForInstallmentSync("inst_1") } returns listOf(firstPayment, secondPayment)

        dataSource.recordInstallmentPayment(
            installmentId = "inst_1",
            amountCents = 6000L,
            paymentDate = 1728604800000L
        )

        coVerify(exactly = 1) {
            installmentDao.updatePayment(
                id = "inst_1",
                paidAt = match { it != null },
                actualPaymentDate = 1728604800000L,
                status = "PAID",
                updatedAt = any()
            )
        }
    }

    @Test
    fun `payInvoiceInstallments inserts payments for each installment and enqueues outbox`() = runTest {
        val insts = listOf(
            sampleInstallment(id = "inst_a", amountCents = 5000L),
            sampleInstallment(id = "inst_b", amountCents = 7000L)
        )

        dataSource.payInvoiceInstallments(
            invoiceId = "inv_123",
            invoiceInsts = insts,
            effectiveActualDate = 1728518400000L
        )

        coVerify(exactly = 1) {
            paymentDao.insertPayments(match { list ->
                list.size == 2 &&
                list[0].billInstallmentId == "inst_a" &&
                list[0].amountCents == 5000L &&
                list[0].notes == "Pagamento da fatura (inv_123)" &&
                list[1].billInstallmentId == "inst_b" &&
                list[1].amountCents == 7000L
            })
        }

        coVerify(exactly = 1) { syncDao.enqueueAll(any()) }
    }

    @Test
    fun `revertInvoicePayments soft deletes invoice payments and enqueues outbox`() = runTest {
        dataSource.revertInvoicePayments("inv_123")

        coVerify(exactly = 1) {
            paymentDao.softDeletePaymentsForInvoice("inv_123", any(), any())
        }
        coVerify(exactly = 1) {
            syncDao.enqueue(match { it.entityType == "PAYMENT" && it.operation == "DELETE_ALL_FOR_INVOICE" })
        }
    }
}
