package com.platform.app.domain.usecase

import androidx.room.withTransaction
import com.platform.app.core.util.DateUtils
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.repository.BillDataSource
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.MonthSummary
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class RecurringLifecycleTest {

    private lateinit var database: PlatformDatabase
    private lateinit var billDao: BillDao
    private lateinit var installmentDao: BillInstallmentDao
    private lateinit var billDataSource: BillDataSource
    private lateinit var calculateInstallmentsUseCase: CalculateInstallmentsUseCase
    private lateinit var extendRecurringBillsUseCase: ExtendRecurringBillsUseCase
    private lateinit var repository: FinancialRepository

    private val referenceNow = 1727740800000L // 01/10/2026

    @Before
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        database = mockk(relaxed = true)
        val transactionLambda = slot<suspend () -> Any>()
        coEvery { database.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
        billDao = mockk(relaxed = true)
        installmentDao = mockk(relaxed = true)
        billDataSource = BillDataSource(database, billDao, installmentDao)
        calculateInstallmentsUseCase = CalculateInstallmentsUseCase()
        repository = mockk(relaxed = true)
        extendRecurringBillsUseCase = ExtendRecurringBillsUseCase(repository, calculateInstallmentsUseCase)
    }

    @Test
    fun `pausing for 3 months marks upcoming in-period installments as SKIPPED while keeping previous overdue as PENDING`() = runTest {
        val billId = "bill-gym"
        val pausedUntil = DateUtils.addMonths(referenceNow, 3)

        billDataSource.pauseRecurringBill(billId, pausedUntil)

        // Verifica que o estado de pausa foi salvo no banco com pausedUntil
        coVerify { billDao.updatePauseState(billId, isPaused = true, pausedUntil = pausedUntil, updatedAt = any()) }

        // Verifica que apenas ocorrências a partir de hoje viram SKIPPED
        coVerify {
            installmentDao.markPendingAsSkippedInRange(
                billId = billId,
                startDate = any(),
                endDate = any(),
                updatedAt = any()
            )
        }
    }

    @Test
    fun `pausing does not hide overdue installments from month summary or calculations`() {
        val yesterday = referenceNow - 24 * 3600 * 1000
        val tomorrow = referenceNow + 24 * 3600 * 1000

        val overdueInst = BillInstallment(
            id = "inst-overdue",
            billId = "bill-1",
            billTitle = "Aluguel Atrasado",
            amountCents = 150000L,
            dueDate = yesterday,
            status = BillStatus.PENDING // Permanece PENDING mesmo com conta pausada
        )

        val skippedInst = BillInstallment(
            id = "inst-skipped",
            billId = "bill-1",
            billTitle = "Aluguel Pulado",
            amountCents = 150000L,
            dueDate = tomorrow,
            status = BillStatus.SKIPPED // Pulada durante o período de pausa
        )

        val summary = MonthSummary.fromInstallments(listOf(overdueInst, skippedInst), nowMillis = referenceNow)

        // Ocorrência atrasada continua sendo contabilizada com exatidão
        assertEquals(150000L, summary.overdueCents)
        assertEquals(1, summary.overdueCount)

        // Ocorrência SKIPPED é ignorada do total e do pendente
        assertEquals(150000L, summary.totalCents)
        assertEquals(0L, summary.pendingCents)
        assertEquals(0, summary.pendingCount)
    }

    @Test
    fun `resuming after 3 months does not turn SKIPPED installments into PENDING (no ghost overdue debts)`() = runTest {
        val billId = "bill-gym"

        billDataSource.resumeRecurringBill(billId)

        // Reseta isPaused e pausedUntil
        coVerify { billDao.updatePauseState(billId, isPaused = false, pausedUntil = null, updatedAt = any()) }

        // Garante que NENHUM método chamado reverte status de parcelas já SKIPPED para PENDING
        coVerify(exactly = 0) { installmentDao.updateStatusForPending(billId, any()) }
    }

    @Test
    fun `ending recurring bill deletes future pending beyond cutoff and sets UNTIL_DATE`() = runTest {
        val billId = "bill-net"
        val nextDue = referenceNow + 15L * 24 * 3600 * 1000

        val mockRemaining = listOf(
            BillInstallmentEntity(
                id = "inst-paid",
                billId = billId,
                installmentNumber = 1,
                totalInstallments = 2,
                amountCents = 10000L,
                dueDate = com.platform.app.core.util.DateUtils.millisToLocalDate(referenceNow - 30L * 24 * 3600 * 1000),
                paidAt = referenceNow - 30L * 24 * 3600 * 1000,
                status = "PAID"
            ),
            BillInstallmentEntity(
                id = "inst-current",
                billId = billId,
                installmentNumber = 2,
                totalInstallments = 2,
                amountCents = 10000L,
                dueDate = com.platform.app.core.util.DateUtils.millisToLocalDate(nextDue),
                paidAt = null,
                status = "PENDING"
            )
        )

        coEvery { installmentDao.getInstallmentsByBillId(billId) } returns mockRemaining

        billDataSource.stopRecurringBill(billId, fromDueDate = nextDue)

        // Verifica que deletou apenas futuras estritamente após a data de corte
        coVerify { installmentDao.deletePendingAfterDueDate(billId, com.platform.app.core.util.DateUtils.millisToLocalDate(nextDue), any(), any()) }

        // Verifica que gravou término definitivo
        coVerify {
            billDao.stopRecurringBillWithDate(
                id = billId,
                recurrenceEndDate = any(),
                endedAt = any(),
                totalInstallments = 2,
                updatedAt = any()
            )
        }
    }

    @Test
    fun `running ExtendRecurringBillsUseCase when recurring bill is ended does not regenerate installments`() = runTest {
        val endedBill = Bill(
            id = "bill-ended",
            title = "Assinatura Cancelada",
            type = BillType.RECURRING,
            totalAmountCents = 5000L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.UNTIL_DATE,
            recurrenceEndDate = referenceNow,
            endedAt = referenceNow
        )

        every { repository.getBills() } returns flowOf(listOf(endedBill))

        val generatedCount = extendRecurringBillsUseCase(referenceTimeMillis = referenceNow)

        assertEquals(0, generatedCount)
        coVerify(exactly = 0) { repository.addInstallments(any(), any()) }
    }

    @Test
    fun `running ExtendRecurringBillsUseCase when recurring bill is paused does not regenerate installments`() = runTest {
        val pausedBill = Bill(
            id = "bill-paused",
            title = "Academia Pausada",
            type = BillType.RECURRING,
            totalAmountCents = 10000L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER,
            isPaused = true,
            pausedUntil = DateUtils.addMonths(referenceNow, 2)
        )

        every { repository.getBills() } returns flowOf(listOf(pausedBill))

        val generatedCount = extendRecurringBillsUseCase(referenceTimeMillis = referenceNow)

        assertEquals(0, generatedCount)
        coVerify(exactly = 0) { repository.addInstallments(any(), any()) }
    }

    @Test
    fun `running ExtendRecurringBillsUseCase when pause has expired automatically resumes and extends`() = runTest {
        val expiredPauseBill = Bill(
            id = "bill-expired-pause",
            title = "Academia Retomada",
            type = BillType.RECURRING,
            totalAmountCents = 10000L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER,
            isPaused = true,
            pausedUntil = referenceNow - 1000L // Pausa já venceu
        )

        every { repository.getBills() } returns flowOf(listOf(expiredPauseBill))
        coEvery { repository.resumeRecurringBill(expiredPauseBill.id) } returns Unit

        // Simula parcelas existentes
        val existingInstallments = listOf(
            BillInstallment(
                id = "inst-old",
                billId = expiredPauseBill.id,
                billTitle = expiredPauseBill.title,
                amountCents = 10000L,
                dueDate = referenceNow - 30L * 24 * 3600 * 1000,
                status = BillStatus.PAID,
                paidAt = referenceNow - 30L * 24 * 3600 * 1000
            )
        )
        coEvery { repository.getInstallmentsByBillId(expiredPauseBill.id) } returns existingInstallments

        val generatedCount = extendRecurringBillsUseCase(referenceTimeMillis = referenceNow)

        // Resumiu automaticamente
        coVerify { repository.resumeRecurringBill(expiredPauseBill.id) }
        assertEquals(12, generatedCount)
    }

    @Test
    fun `reactivating an ended recurring bill resets end date and allows future extensions`() = runTest {
        val billId = "bill-reactivate"

        billDataSource.reactivateRecurringBill(billId)

        coVerify { billDao.reactivateRecurringBill(billId, any()) }
    }

    @Test
    fun `deleting recurring bill with keepPaidHistory soft-deletes bill and removes only pending installments`() = runTest {
        val billId = "bill-del-soft"

        billDataSource.deleteRecurringBill(billId, keepPaidHistory = true)

        coVerify { installmentDao.deleteAllPending(billId, any(), any()) }
        coVerify { billDao.markBillEndedAndDeleted(billId, any()) }
        coVerify(exactly = 0) { billDao.deleteById(billId) }
    }
}
