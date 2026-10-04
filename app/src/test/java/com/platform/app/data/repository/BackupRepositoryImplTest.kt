package com.platform.app.data.repository

import com.platform.app.core.dispatcher.DispatcherProvider
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.BudgetDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.dao.ExpenseItemDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.GoalDao
import com.platform.app.data.local.dao.PaymentMethodDao
import com.platform.app.data.local.dao.TransactionDao
import com.platform.app.data.local.entity.CategoryEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackupRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
        override val unconfined: CoroutineDispatcher = testDispatcher
    }

    private lateinit var database: PlatformDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var expenseItemDao: ExpenseItemDao
    private lateinit var creditCardDao: CreditCardDao
    private lateinit var financialAccountDao: FinancialAccountDao
    private lateinit var paymentMethodDao: PaymentMethodDao
    private lateinit var contactDao: ContactDao
    private lateinit var billDao: BillDao
    private lateinit var billInstallmentDao: BillInstallmentDao
    private lateinit var goalDao: GoalDao
    private lateinit var budgetDao: BudgetDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var repository: BackupRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        database = mockk(relaxed = true)
        categoryDao = mockk(relaxed = true)
        expenseItemDao = mockk(relaxed = true)
        creditCardDao = mockk(relaxed = true)
        financialAccountDao = mockk(relaxed = true)
        paymentMethodDao = mockk(relaxed = true)
        contactDao = mockk(relaxed = true)
        billDao = mockk(relaxed = true)
        billInstallmentDao = mockk(relaxed = true)
        goalDao = mockk(relaxed = true)
        budgetDao = mockk(relaxed = true)
        transactionDao = mockk(relaxed = true)

        repository = BackupRepositoryImpl(
            database = database,
            categoryDao = categoryDao,
            expenseItemDao = expenseItemDao,
            creditCardDao = creditCardDao,
            financialAccountDao = financialAccountDao,
            paymentMethodDao = paymentMethodDao,
            contactDao = contactDao,
            billDao = billDao,
            billInstallmentDao = billInstallmentDao,
            goalDao = goalDao,
            budgetDao = budgetDao,
            transactionDao = transactionDao,
            dispatcherProvider = dispatcherProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exportBackupJson successfully exports structured JSON containing version and entities`() = runTest(testDispatcher) {
        val sampleCategory = CategoryEntity(
            id = "cat-1",
            name = "Alimentação",
            colorHex = "#FF5722",
            iconName = "Restaurant"
        )
        coEvery { categoryDao.getAllList() } returns listOf(sampleCategory)
        coEvery { expenseItemDao.getAllList() } returns emptyList()
        coEvery { creditCardDao.getAllCardsList() } returns emptyList()

        val result = repository.exportBackupJson()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        val json = result.getOrThrow()
        assertTrue(json.contains("\"version\": 2"))
        assertTrue(json.contains("Alimentação"))
    }

    @Test
    fun `restoreBackupFromJson returns failure when json is corrupt or empty`() = runTest(testDispatcher) {
        val result = repository.restoreBackupFromJson("")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isFailure)
    }
}
