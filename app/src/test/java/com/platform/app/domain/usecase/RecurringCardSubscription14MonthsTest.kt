package com.platform.app.domain.usecase

import androidx.room.withTransaction
import com.platform.app.core.util.DateUtils
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.dao.ExpenseItemDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.PaymentMethodDao
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.CreditCardEntity
import com.platform.app.data.local.entity.CreditCardInvoiceEntity
import com.platform.app.data.repository.FinancialRepositoryImpl
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class RecurringCardSubscription14MonthsTest {

    private lateinit var database: PlatformDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var expenseItemDao: ExpenseItemDao
    private lateinit var creditCardDao: CreditCardDao
    private lateinit var contactDao: ContactDao
    private lateinit var financialAccountDao: FinancialAccountDao
    private lateinit var paymentMethodDao: PaymentMethodDao
    private lateinit var billDao: BillDao
    private lateinit var installmentDao: BillInstallmentDao
    private lateinit var repository: FinancialRepositoryImpl

    private lateinit var calculateInstallmentsUseCase: CalculateInstallmentsUseCase
    private lateinit var createBillUseCase: CreateBillUseCase

    private val savedBills = mutableListOf<BillEntity>()
    private val savedInstallments = mutableListOf<BillInstallmentEntity>()
    private val savedInvoices = mutableListOf<CreditCardInvoiceEntity>()

    private val sampleCard = CreditCard(
        id = "card-nubank",
        name = "Nubank Ultravioleta",
        totalLimitCents = 1500000L,
        closingDay = 20,
        dueDay = 27
    )

    private val sampleCardEntity = CreditCardEntity(
        id = sampleCard.id,
        name = sampleCard.name,
        totalLimitCents = sampleCard.totalLimitCents,
        closingDay = sampleCard.closingDay,
        dueDay = sampleCard.dueDay,
        colorHex = "#820AD1"
    )

    @Before
    fun setUp() {
        savedBills.clear()
        savedInstallments.clear()
        savedInvoices.clear()

        database = mockk(relaxed = true)
        categoryDao = mockk(relaxed = true)
        expenseItemDao = mockk(relaxed = true)
        creditCardDao = mockk(relaxed = true)
        contactDao = mockk(relaxed = true)
        financialAccountDao = mockk(relaxed = true)
        paymentMethodDao = mockk(relaxed = true)
        billDao = mockk(relaxed = true)
        installmentDao = mockk(relaxed = true)

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Any>()
        coEvery { database.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        // Simulação do repositório e DAOs em memória
        coEvery { creditCardDao.getCardById(sampleCard.id) } returns sampleCardEntity
        coEvery { creditCardDao.getAllCardsList() } returns listOf(sampleCardEntity)
        coEvery { creditCardDao.getAllCards() } returns flowOf(listOf(sampleCardEntity))

        coEvery { creditCardDao.getInvoiceByMonth(sampleCard.id, any()) } answers {
            val month = secondArg<String>()
            savedInvoices.find { it.creditCardId == sampleCard.id && it.referenceMonth == month }
        }

        coEvery { creditCardDao.insertInvoice(any()) } answers {
            val inv = firstArg<CreditCardInvoiceEntity>()
            savedInvoices.removeIf { it.id == inv.id }
            savedInvoices.add(inv)
        }

        coEvery { billDao.insert(any()) } answers {
            val bill = firstArg<BillEntity>()
            savedBills.removeIf { it.id == bill.id }
            savedBills.add(bill)
        }

        coEvery { installmentDao.insertAll(any()) } answers {
            val list = firstArg<List<BillInstallmentEntity>>()
            savedInstallments.addAll(list)
        }

        coEvery { installmentDao.getUnattachedRecurringInstallmentsForCard(sampleCard.id) } answers {
            savedInstallments.filter { it.invoiceId == null }
        }

        coEvery { installmentDao.attachInstallmentsToInvoice(any(), any(), any()) } answers {
            val ids = firstArg<List<String>>()
            val invoiceId = secondArg<String>()
            val dueDate = thirdArg<Long>()
            for (i in savedInstallments.indices) {
                if (savedInstallments[i].id in ids) {
                    savedInstallments[i] = savedInstallments[i].copy(
                        invoiceId = invoiceId,
                        dueDate = dueDate
                    )
                }
            }
        }

        repository = FinancialRepositoryImpl(
            database = database,
            categoryDao = categoryDao,
            expenseItemDao = expenseItemDao,
            creditCardDao = creditCardDao,
            contactDao = contactDao,
            financialAccountDao = financialAccountDao,
            paymentMethodDao = paymentMethodDao,
            billDao = billDao,
            installmentDao = installmentDao,
            preferencesManager = mockk(relaxed = true)
        )

        calculateInstallmentsUseCase = CalculateInstallmentsUseCase()
        createBillUseCase = CreateBillUseCase(repository, calculateInstallmentsUseCase)
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `monthly card subscription for 14 months should have exactly 1 occurrence per invoice, stable due dates, and no extended occurrence pointing to first invoice`() = runTest {
        // 1. DATA BASE: 10 de Outubro de 2026 (dia 10, antes do fechamento dia 20)
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.OCTOBER, 10, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val subscriptionStartDate = cal.timeInMillis

        val subscriptionBill = Bill(
            id = "bill-netflix-14m",
            title = "Netflix Premium",
            type = BillType.RECURRING,
            totalAmountCents = 5590L,
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceEndType = RecurrenceEndType.FOREVER,
            recurrenceAnchorDate = subscriptionStartDate,
            creditCardId = sampleCard.id
        )

        // 2. CRIAÇÃO DA DESPESA (Lote inicial de 12 meses)
        val initialInstallments = createBillUseCase(
            bill = subscriptionBill,
            firstDueDate = subscriptionStartDate,
            creditCard = sampleCard
        )

        assertEquals(12, initialInstallments.size)
        assertEquals(12, savedInstallments.size)

        // Primeira fatura ("2026-10") foi criada de imediato no ciclo 1
        val firstInvoice = savedInvoices.find { it.referenceMonth == "2026-10" }
        assertNotNull("Fatura 2026-10 deve existir", firstInvoice)
        val firstInvoiceId = firstInvoice!!.id

        // Ocorrência 1 está vinculada à 1ª fatura com vencimento trocado para o vencimento da fatura (dia 27)
        val firstInst = savedInstallments[0]
        assertEquals(firstInvoiceId, firstInst.invoiceId)
        val calFirstDue = Calendar.getInstance().apply { timeInMillis = firstInst.dueDate }
        assertEquals(27, calFirstDue.get(Calendar.DAY_OF_MONTH))

        // Ocorrências 2 a 12 (meses 2 a 12) têm invoiceId nulo e dia de vencimento original estável no dia 10
        for (i in 1 until 12) {
            val inst = savedInstallments[i]
            assertNull("Ocorrência ${i + 1} deve ter invoiceId nulo inicialmente", inst.invoiceId)
            val instCal = Calendar.getInstance().apply { timeInMillis = inst.dueDate }
            assertEquals("Ocorrência ${i + 1} deve manter dia de vencimento estável no dia 10", 10, instCal.get(Calendar.DAY_OF_MONTH))
        }

        // 3. EXTENSÃO DA JANELA: gerar meses 13 e 14
        val billFromDb = savedBills.first().toDomain()
        assertEquals(subscriptionStartDate, billFromDb.recurrenceAnchorDate)

        val extendedBatch = calculateInstallmentsUseCase.generateNextRecurringInstallments(
            bill = billFromDb,
            existingInstallments = savedInstallments.map { it.toDomain("Netflix", null, "", "", "category", com.platform.app.domain.model.ExpenseNature.NECESSARIO, null, null, null, null, BillType.RECURRING) },
            occurrencesToAdd = 2
        )

        assertEquals(2, extendedBatch.size)
        val inst13 = extendedBatch[0]
        val inst14 = extendedBatch[1]

        assertEquals(13, inst13.installmentNumber)
        assertEquals(14, inst14.installmentNumber)

        // NENHUMA ocorrência estendida aponta para a 1ª fatura
        assertNull("Ocorrência 13 não deve apontar para a 1ª fatura", inst13.invoiceId)
        assertNull("Ocorrência 14 não deve apontar para a 1ª fatura", inst14.invoiceId)
        assertNotEquals(firstInvoiceId, inst13.invoiceId)
        assertNotEquals(firstInvoiceId, inst14.invoiceId)

        // O dia de vencimento das ocorrências estendidas é rigorosamente estável (dia 10)
        val cal13 = Calendar.getInstance().apply { timeInMillis = inst13.dueDate }
        val cal14 = Calendar.getInstance().apply { timeInMillis = inst14.dueDate }
        assertEquals("Ocorrência 13 deve manter dia 10", 10, cal13.get(Calendar.DAY_OF_MONTH))
        assertEquals("Ocorrência 14 deve manter dia 10", 10, cal14.get(Calendar.DAY_OF_MONTH))
        assertEquals(2027, cal13.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, cal13.get(Calendar.MONTH))
        assertEquals(2027, cal14.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, cal14.get(Calendar.MONTH))

        // Adiciona ocorrências 13 e 14 ao repositório
        savedInstallments.addAll(extendedBatch.map { BillInstallmentEntity.fromDomain(it) })
        assertEquals(14, savedInstallments.size)

        // 4. MATERIALIZAÇÃO SOB DEMANDA AO LONGO DOS 14 MESES
        // Simulando a passagem dos meses 1 a 14: abrindo a fatura de cada competência
        val expectedReferenceMonths = listOf(
            "2026-10", "2026-11", "2026-12",
            "2027-01", "2027-02", "2027-03", "2027-04", "2027-05", "2027-06", "2027-07", "2027-08", "2027-09",
            "2027-10", "2027-11"
        )

        for (refMonth in expectedReferenceMonths.drop(1)) {
            // Chama a materialização sob demanda da fatura daquele mês
            val invoice = repository.getOrCreateInvoiceForMonth(sampleCard.id, refMonth)
            assertNotNull("Fatura $refMonth deve ser gerada sob demanda", invoice)
            assertNotEquals("Fatura $refMonth deve ter ID exclusivo", firstInvoiceId, invoice.id)
        }

        // 5. VALIDAÇÕES FINAIS:
        // A) Existem exatamente 14 faturas geradas no cartão
        assertEquals(14, savedInvoices.size)

        // B) Cada fatura contém EXATAMENTE 1 ocorrência
        for (refMonth in expectedReferenceMonths) {
            val invoice = savedInvoices.find { it.referenceMonth == refMonth }
            assertNotNull("Fatura $refMonth deve existir", invoice)
            val installmentsForThisInvoice = savedInstallments.filter { it.invoiceId == invoice!!.id }
            assertEquals(
                "Fatura $refMonth deve conter exatamente 1 ocorrência",
                1,
                installmentsForThisInvoice.size
            )
        }

        // C) Nenhuma das 14 ocorrências ficou com invoiceId nulo
        savedInstallments.forEach {
            assertNotNull("Todas as parcelas devem estar vinculadas à sua respectiva fatura", it.invoiceId)
        }

        // D) Nenhuma ocorrência dos meses 2 a 14 aponta para a 1ª fatura
        for (i in 1 until 14) {
            val inst = savedInstallments[i]
            assertNotEquals(
                "Parcela ${i + 1} não deve apontar para a 1ª fatura",
                firstInvoiceId,
                inst.invoiceId
            )
        }

        // E) Na fatura, o vencimento de todas as 14 parcelas está padronizado no vencimento da fatura (dia 27)
        for (inst in savedInstallments) {
            val instCal = Calendar.getInstance().apply { timeInMillis = inst.dueDate }
            assertEquals("Vencimento na fatura deve ser dia 27", 27, instCal.get(Calendar.DAY_OF_MONTH))
        }
    }
}
