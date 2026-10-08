package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Collections

class ExtendRecurringBillsUseCaseTest {

    private lateinit var repository: FinancialRepository
    private lateinit var calculateInstallmentsUseCase: CalculateInstallmentsUseCase
    private lateinit var useCase: ExtendRecurringBillsUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        calculateInstallmentsUseCase = CalculateInstallmentsUseCase()
        useCase = ExtendRecurringBillsUseCase(repository, calculateInstallmentsUseCase)
    }

    @Test
    fun `when FOREVER bill is near window end, it generates 12 new installments and updates repository`() = runTest {
        val referenceNow = 1727395200000L // Data base de teste
        val bill = Bill(
            id = "bill-1",
            title = "Academia",
            type = BillType.RECURRING,
            totalAmountCents = 10000L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER,
            totalInstallments = 12
        )

        // Simula que a conta começou há 10 meses e só restam 2 parcelas no futuro (11 e 12)
        val startDate = DateUtils.addMonths(referenceNow, -10)
        val initialInstallments = calculateInstallmentsUseCase(bill, startDate).mapIndexed { index, inst ->
            if (index < 10) inst.copy(status = BillStatus.PAID, paidAt = inst.dueDate)
            else inst
        }

        every { repository.getBills() } returns flowOf(listOf(bill))
        coEvery { repository.getInstallmentsByBillId(bill.id) } returns initialInstallments

        val addedInstallmentsSlot = mutableListOf<List<BillInstallment>>()
        coEvery { repository.addInstallments(bill, capture(addedInstallmentsSlot)) } returns Unit

        val countGenerated = useCase(referenceTimeMillis = referenceNow)

        assertEquals(12, countGenerated)
        coVerify(atLeast = 1) { repository.addInstallments(bill, any()) }

        val addedList = addedInstallmentsSlot.first()
        assertEquals(12, addedList.size)
        assertEquals(13, addedList.first().installmentNumber)
        assertEquals(24, addedList.last().installmentNumber)
        assertTrue(addedList.last().dueDate > referenceNow)
    }

    @Test
    fun `when FOREVER bill has plenty of future installments, it does not extend (idempotency)`() = runTest {
        val referenceNow = 1727395200000L
        val bill = Bill(
            id = "bill-2",
            title = "Netflix",
            type = BillType.RECURRING,
            totalAmountCents = 5590L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER,
            totalInstallments = 12
        )

        // Criada hoje: tem 12 parcelas futuras completas (muito acima do limiar de 3)
        val initialInstallments = calculateInstallmentsUseCase(bill, referenceNow)

        every { repository.getBills() } returns flowOf(listOf(bill))
        coEvery { repository.getInstallmentsByBillId(bill.id) } returns initialInstallments

        val countGenerated = useCase(referenceTimeMillis = referenceNow)

        assertEquals(0, countGenerated)
        coVerify(exactly = 0) { repository.addInstallments(any(), any()) }
    }

    @Test
    fun `non-FOREVER recurring bills (BY_OCCURRENCES or UNTIL_DATE) are never extended`() = runTest {
        val referenceNow = 1727395200000L
        val billByOccurrences = Bill(
            id = "bill-3",
            title = "Curso 6 Meses",
            type = BillType.RECURRING,
            totalAmountCents = 20000L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.BY_OCCURRENCES,
            totalInstallments = 6
        )

        val billUntilDate = Bill(
            id = "bill-4",
            title = "Contrato Temporário",
            type = BillType.RECURRING,
            totalAmountCents = 30000L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.UNTIL_DATE,
            recurrenceEndDate = DateUtils.addMonths(referenceNow, 1)
        )

        every { repository.getBills() } returns flowOf(listOf(billByOccurrences, billUntilDate))

        val countGenerated = useCase(referenceTimeMillis = referenceNow)

        assertEquals(0, countGenerated)
        coVerify(exactly = 0) { repository.addInstallments(any(), any()) }
    }

    @Test
    fun `single and installment bills are ignored`() = runTest {
        val referenceNow = 1727395200000L
        val singleBill = Bill(id = "s1", title = "Luz", type = BillType.SINGLE, totalAmountCents = 1000L)
        val instBill = Bill(id = "i1", title = "Celular", type = BillType.INSTALLMENT, totalAmountCents = 1000L, totalInstallments = 10)

        every { repository.getBills() } returns flowOf(listOf(singleBill, instBill))

        val countGenerated = useCase(referenceTimeMillis = referenceNow)

        assertEquals(0, countGenerated)
        coVerify(exactly = 0) { repository.addInstallments(any(), any()) }
    }

    @Test
    fun `two concurrent async calls to extendRecurringBillsUseCase do not generate duplicate installments`() = runTest {
        val referenceNow = 1727395200000L
        val bill = Bill(
            id = "bill-concurrent",
            title = "Academia Concorrente",
            type = BillType.RECURRING,
            totalAmountCents = 10000L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER,
            totalInstallments = 12
        )

        val startDate = DateUtils.addMonths(referenceNow, -10)
        val initialInstallments = calculateInstallmentsUseCase(bill, startDate).mapIndexed { index, inst ->
            if (index < 10) inst.copy(status = BillStatus.PAID, paidAt = inst.dueDate)
            else inst
        }

        val storedInstallments = Collections.synchronizedList(initialInstallments.toMutableList())

        every { repository.getBills() } returns flowOf(listOf(bill))
        coEvery { repository.getInstallmentsByBillId(bill.id) } answers {
            synchronized(storedInstallments) { storedInstallments.toList() }
        }
        coEvery { repository.addInstallments(bill, any()) } answers {
            val newBatch = secondArg<List<BillInstallment>>()
            synchronized(storedInstallments) {
                storedInstallments.addAll(newBatch)
            }
        }

        // Duas chamadas concorrentes assíncronas com referência idêntica
        val deferred1 = async(Dispatchers.Default) { useCase(referenceNow) }
        val deferred2 = async(Dispatchers.Default) { useCase(referenceNow) }

        val res1 = deferred1.await()
        val res2 = deferred2.await()

        // Graças ao Mutex.withLock no useCase @Singleton, exatamente um estende e o outro vê o banco já estendido
        val totalCountGenerated = res1 + res2
        assertEquals(12, totalCountGenerated)

        synchronized(storedInstallments) {
            assertEquals(24, storedInstallments.size)
            val uniqueInstallmentNumbers = storedInstallments.map { it.installmentNumber }.toSet()
            assertEquals(24, uniqueInstallmentNumbers.size)
        }
    }
}
