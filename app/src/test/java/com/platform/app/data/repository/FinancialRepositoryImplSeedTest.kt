package com.platform.app.data.repository

import androidx.room.withTransaction
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.dao.ExpenseItemDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.PaymentMethodDao
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
import org.junit.Before
import org.junit.Test

class FinancialRepositoryImplSeedTest {

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
    fun `seedInitialData when seedsApplied is true does not recreate records even if user deleted all data`() = runTest {
        // Simula que seeds já foram aplicadas no passado
        every { preferencesManager.areSeedsApplied } returns flowOf(true)
        // Simula que o usuário excluiu todos os dados de todas as tabelas
        coEvery { categoryDao.count() } returns 0
        coEvery { financialAccountDao.count() } returns 0

        repository.seedInitialData()

        // Garante que absolutamente nada foi recriado
        coVerify(exactly = 0) { categoryDao.insertAll(any()) }
        coVerify(exactly = 0) { expenseItemDao.insertAll(any()) }
        coVerify(exactly = 0) { contactDao.insertAll(any()) }
        coVerify(exactly = 0) { financialAccountDao.insertAll(any()) }
        coVerify(exactly = 0) { paymentMethodDao.insertAll(any()) }
        coVerify(exactly = 0) { creditCardDao.insertCard(any()) }
        coVerify(exactly = 0) { creditCardDao.insertAllCards(any()) }
        coVerify(exactly = 0) { preferencesManager.setSeedsApplied(any()) }
    }

    @Test
    fun `seedInitialData when seedsApplied is false and tables empty seeds default data without any credit cards`() = runTest {
        // Simula primeiro uso
        every { preferencesManager.areSeedsApplied } returns flowOf(false)
        coEvery { categoryDao.count() } returns 0
        coEvery { financialAccountDao.count() } returns 0

        repository.seedInitialData()

        // Garante que os dados padrão foram semeados
        coVerify(exactly = 1) { categoryDao.insertAll(any()) }
        coVerify(exactly = 1) { expenseItemDao.insertAll(any()) }
        coVerify(exactly = 1) { contactDao.insertAll(any()) }
        coVerify(exactly = 1) { financialAccountDao.insertAll(any()) }
        coVerify(exactly = 1) { paymentMethodDao.insertAll(any()) }

        // Garante que NENHUM cartão de crédito fictício foi criado
        coVerify(exactly = 0) { creditCardDao.insertCard(any()) }
        coVerify(exactly = 0) { creditCardDao.insertAllCards(any()) }

        // Garante que a flag de controle foi persistida como true
        coVerify(exactly = 1) { preferencesManager.setSeedsApplied(true) }
    }

    @Test
    fun `seedInitialData when seedsApplied is false but user has existing data skips seeding and sets flag`() = runTest {
        // Usuário existente que atualizou o app: flag ainda false, mas já tem dados no banco
        every { preferencesManager.areSeedsApplied } returns flowOf(false)
        coEvery { categoryDao.count() } returns 3
        coEvery { financialAccountDao.count() } returns 1

        repository.seedInitialData()

        // Garante que nenhum seed sobrescreveu dados do usuário
        coVerify(exactly = 0) { categoryDao.insertAll(any()) }
        coVerify(exactly = 0) { expenseItemDao.insertAll(any()) }
        coVerify(exactly = 0) { contactDao.insertAll(any()) }
        coVerify(exactly = 0) { financialAccountDao.insertAll(any()) }
        coVerify(exactly = 0) { paymentMethodDao.insertAll(any()) }

        // Garante que a flag é atualizada para true para não verificar novamente
        coVerify(exactly = 1) { preferencesManager.setSeedsApplied(true) }
    }
}
