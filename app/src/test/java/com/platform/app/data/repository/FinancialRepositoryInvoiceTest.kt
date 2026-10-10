package com.platform.app.data.repository

import androidx.room.withTransaction
import app.cash.turbine.test
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.dao.ExpenseItemDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.InvoiceTotal
import com.platform.app.data.local.dao.PaymentMethodDao
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.CreditCardEntity
import com.platform.app.data.local.entity.CreditCardInvoiceEntity
import com.platform.app.domain.model.InvoiceStatus
import java.util.Calendar
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FinancialRepositoryInvoiceTest {

    private lateinit var database: PlatformDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var expenseItemDao: ExpenseItemDao
    private lateinit var creditCardDao: CreditCardDao
    private lateinit var contactDao: ContactDao
    private lateinit var financialAccountDao: FinancialAccountDao
    private lateinit var paymentMethodDao: PaymentMethodDao
    private lateinit var billDao: BillDao
    private lateinit var installmentDao: BillInstallmentDao
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var repository: FinancialRepositoryImpl

    @Before
    fun setUp() {
        database = mockk(relaxed = true)
        categoryDao = mockk(relaxed = true)
        expenseItemDao = mockk(relaxed = true)
        creditCardDao = mockk(relaxed = true)
        contactDao = mockk(relaxed = true)
        financialAccountDao = mockk(relaxed = true)
        paymentMethodDao = mockk(relaxed = true)
        billDao = mockk(relaxed = true)
        installmentDao = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Any>()
        coEvery { database.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
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
            preferencesManager = preferencesManager
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `payInvoice should update status and mark only installments of target invoice with custom actualPaymentDate`() = runTest {
        val targetInvoiceId = "inv-target-1"
        val customActualPaymentDate = 1700000000000L

        repository.payInvoice(
            invoiceId = targetInvoiceId,
            actualPaymentDate = customActualPaymentDate
        )

        // Verifica que a fatura foi marcada como PAGA
        coVerify(exactly = 1) {
            creditCardDao.updateInvoiceStatus(targetInvoiceId, InvoiceStatus.PAGA.name)
        }

        // Verifica que a query SQL direta atualizou apenas as parcelas desta fatura com a data real fornecida
        coVerify(exactly = 1) {
            installmentDao.updatePaymentByInvoiceId(
                invoiceId = targetInvoiceId,
                paidAt = any(),
                actualPaymentDate = customActualPaymentDate,
                status = "PAID"
            )
        }

        // Garante que outras faturas não foram tocadas
        coVerify(exactly = 0) {
            installmentDao.updatePaymentByInvoiceId(
                invoiceId = "inv-other-2",
                paidAt = any(),
                actualPaymentDate = any(),
                status = any()
            )
        }

        // Garante que NÃO houve carregamento da tabela completa em memória (princípio do Resíduo Zero / Room Performance)
        coVerify(exactly = 0) {
            installmentDao.getAllInstallmentsList()
        }
    }

    @Test
    fun `payInvoice without actualPaymentDate should default actualPaymentDate to current timestamp`() = runTest {
        val targetInvoiceId = "inv-target-1"
        val paidAtSlot = slot<Long>()
        val actualDateSlot = slot<Long>()

        repository.payInvoice(invoiceId = targetInvoiceId)

        coVerify(exactly = 1) {
            creditCardDao.updateInvoiceStatus(targetInvoiceId, InvoiceStatus.PAGA.name)
            installmentDao.updatePaymentByInvoiceId(
                invoiceId = targetInvoiceId,
                paidAt = capture(paidAtSlot),
                actualPaymentDate = capture(actualDateSlot),
                status = "PAID"
            )
        }

        assertNotNull(paidAtSlot.captured)
        assertNotNull(actualDateSlot.captured)
        // Quando omitido, a data real é o próprio momento do pagamento (hoje)
        assertEquals(paidAtSlot.captured, actualDateSlot.captured)
    }

    @Test
    fun `reopenInvoice should update status and reset only installments of target invoice`() = runTest {
        val targetInvoiceId = "inv-target-1"

        repository.reopenInvoice(invoiceId = targetInvoiceId)

        // Verifica status da fatura aberto
        coVerify(exactly = 1) {
            creditCardDao.updateInvoiceStatus(targetInvoiceId, InvoiceStatus.ABERTA.name)
        }

        // Verifica que apenas a fatura alvo teve as parcelas resetadas
        coVerify(exactly = 1) {
            installmentDao.updatePaymentByInvoiceId(
                invoiceId = targetInvoiceId,
                paidAt = null,
                actualPaymentDate = null,
                status = "PENDING"
            )
        }

        // Garante que faturas de outros cartões ou meses não foram alteradas
        coVerify(exactly = 0) {
            installmentDao.updatePaymentByInvoiceId(
                invoiceId = "inv-other-9",
                paidAt = any(),
                actualPaymentDate = any(),
                status = any()
            )
        }

        // Garante ausência de filtro em memória
        coVerify(exactly = 0) {
            installmentDao.getAllInstallmentsList()
        }
    }

    @Test
    fun `getCreditCardInvoices should combine invoices with SQL invoice totals efficiently`() = runTest {
        val cardId = "card-nubank"
        val invoice1 = CreditCardInvoiceEntity(
            id = "inv-1",
            creditCardId = cardId,
            referenceMonth = "2026-10",
            closingDate = 1000L,
            dueDate = 2000L,
            status = InvoiceStatus.ABERTA.name
        )
        val invoice2 = CreditCardInvoiceEntity(
            id = "inv-2",
            creditCardId = cardId,
            referenceMonth = "2026-11",
            closingDate = 3000L,
            dueDate = 4000L,
            status = InvoiceStatus.ABERTA.name
        )

        every { creditCardDao.getInvoicesForCard(cardId) } returns flowOf(listOf(invoice1, invoice2))
        every { installmentDao.getInvoiceTotals() } returns flowOf(
            listOf(
                InvoiceTotal(invoiceId = "inv-1", totalAmountCents = 150000L), // R$ 1.500,00
                InvoiceTotal(invoiceId = "inv-other", totalAmountCents = 99999L) // Outro cartão
            )
        )

        repository.getCreditCardInvoices(cardId).test {
            val result = awaitItem()
            assertEquals(2, result.size)

            val domainInv1 = result.find { it.id == "inv-1" }
            assertNotNull(domainInv1)
            assertEquals(150000L, domainInv1?.totalAmountCents)

            // inv-2 não tem parcelas registradas -> totalAmountCents deve ser 0L
            val domainInv2 = result.find { it.id == "inv-2" }
            assertNotNull(domainInv2)
            assertEquals(0L, domainInv2?.totalAmountCents)

            awaitComplete()
        }

        // Garante que getAllWithDetails (7 joins em memória) NÃO foi chamado
        coVerify(exactly = 0) {
            installmentDao.getAllWithDetails()
        }
    }

    @Test
    fun `getOrCreateInvoiceForMonth should attach unattached recurring installments for matching reference month`() = runTest {
        val cardId = "card-1"
        val card = CreditCardEntity(
            id = cardId,
            name = "Visa",
            totalLimitCents = 500000L,
            closingDay = 20,
            dueDay = 27
        )
        coEvery { creditCardDao.getCardById(cardId) } returns card
        coEvery { creditCardDao.getInvoiceByMonth(cardId, "2026-11") } returns null

        val capturedInvoice = slot<CreditCardInvoiceEntity>()
        coEvery { creditCardDao.insertInvoice(capture(capturedInvoice)) } returns Unit

        // Timestamp dia 10 de Novembro de 2026 (ciclo "2026-11")
        val calNov = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 10, 12, 0, 0)
        }
        // Timestamp dia 10 de Dezembro de 2026 (ciclo "2026-12")
        val calDec = Calendar.getInstance().apply {
            set(2026, Calendar.DECEMBER, 10, 12, 0, 0)
        }

        val unattachedNov = BillInstallmentEntity(
            id = "inst-nov",
            billId = "bill-1",
            installmentNumber = 2,
            totalInstallments = 12,
            amountCents = 3990L,
            dueDate = calNov.timeInMillis,
            paidAt = null,
            status = "PENDING",
            invoiceId = null
        )
        val unattachedDec = BillInstallmentEntity(
            id = "inst-dec",
            billId = "bill-1",
            installmentNumber = 3,
            totalInstallments = 12,
            amountCents = 3990L,
            dueDate = calDec.timeInMillis,
            paidAt = null,
            status = "PENDING",
            invoiceId = null
        )

        coEvery { installmentDao.getUnattachedRecurringInstallmentsForCard(cardId) } returns listOf(unattachedNov, unattachedDec)
        coEvery { installmentDao.attachInstallmentsToInvoice(any(), any(), any()) } returns Unit

        val invoice = repository.getOrCreateInvoiceForMonth(cardId, "2026-11")

        assertNotNull(invoice)
        assertEquals("2026-11", invoice.referenceMonth)

        // Deve ter anexado apenas inst-nov, não inst-dec
        coVerify(exactly = 1) {
            installmentDao.attachInstallmentsToInvoice(
                installmentIds = listOf("inst-nov"),
                invoiceId = invoice.id,
                invoiceDueDate = invoice.dueDate
            )
        }
    }

    @Test
    fun `materializeRecurringCardInvoices should process all cards and attach occurrences whose cycle has arrived`() = runTest {
        val cardId = "card-1"
        val card = CreditCardEntity(
            id = cardId,
            name = "Visa",
            totalLimitCents = 500000L,
            closingDay = 20,
            dueDay = 27
        )
        coEvery { creditCardDao.getAllCardsList() } returns listOf(card)
        coEvery { creditCardDao.getCardById(cardId) } returns card

        // Referência atual: 15 de Novembro de 2026 -> currentRefMonth é "2026-11"
        val calRefNow = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 15, 12, 0, 0)
        }
        val refTime = calRefNow.timeInMillis

        val calOct = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 10, 12, 0, 0) }
        val calNov = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 10, 12, 0, 0) }
        val calDec = Calendar.getInstance().apply { set(2026, Calendar.DECEMBER, 10, 12, 0, 0) }

        val instOct = BillInstallmentEntity(
            id = "inst-oct", billId = "b1", installmentNumber = 1, totalInstallments = 12,
            amountCents = 5000L, dueDate = calOct.timeInMillis, paidAt = null, status = "PENDING", invoiceId = null
        )
        val instNov = BillInstallmentEntity(
            id = "inst-nov", billId = "b1", installmentNumber = 2, totalInstallments = 12,
            amountCents = 5000L, dueDate = calNov.timeInMillis, paidAt = null, status = "PENDING", invoiceId = null
        )
        val instDec = BillInstallmentEntity(
            id = "inst-dec", billId = "b1", installmentNumber = 3, totalInstallments = 12,
            amountCents = 5000L, dueDate = calDec.timeInMillis, paidAt = null, status = "PENDING", invoiceId = null
        )

        coEvery { installmentDao.getUnattachedRecurringInstallmentsForCard(cardId) } returns listOf(instOct, instNov, instDec)
        coEvery { creditCardDao.getInvoiceByMonth(cardId, any()) } returns null
        coEvery { creditCardDao.insertInvoice(any()) } returns Unit
        coEvery { installmentDao.attachInstallmentsToInvoice(any(), any(), any()) } returns Unit

        val count = repository.materializeRecurringCardInvoices(refTime)

        // Outubro ("2026-10") e Novembro ("2026-11") são <= "2026-11" -> devem ser materializados (2 parcelas)
        // Dezembro ("2026-12") é futuro -> NÃO deve ser materializado ainda
        assertEquals(2, count)
        coVerify(exactly = 1) { creditCardDao.insertInvoice(match { it.referenceMonth == "2026-10" }) }
        coVerify(exactly = 1) { creditCardDao.insertInvoice(match { it.referenceMonth == "2026-11" }) }
        coVerify(exactly = 0) { creditCardDao.insertInvoice(match { it.referenceMonth == "2026-12" }) }
    }

    @Test
    fun `updateInstallmentsActualPaymentDateBatch should forward list of ids and actualPaymentDate to DAO`() = runTest {
        val ids = listOf("inst-1", "inst-2", "inst-3")
        val customDate = 1759000000000L

        repository.updateInstallmentsActualPaymentDateBatch(ids, customDate)

        coVerify(exactly = 1) { installmentDao.updateActualPaymentDateBatch(ids, customDate) }
    }
}
