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
import com.platform.app.data.local.entity.CreditCardInvoiceEntity
import com.platform.app.domain.model.InvoiceStatus
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
}
