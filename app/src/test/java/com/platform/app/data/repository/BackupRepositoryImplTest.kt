package com.platform.app.data.repository

import androidx.room.withTransaction
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
import com.platform.app.data.local.entity.CategoryEntity
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import com.google.gson.Gson
import com.platform.app.data.local.backup.BackupDataDto
import com.platform.app.data.local.backup.EncryptedBackupDto
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackupRepositoryImplTest {

    private val gson = Gson()
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
    private lateinit var repository: BackupRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("androidx.room.RoomDatabaseKt")
        database = mockk(relaxed = true)
        coEvery { database.withTransaction(any<suspend () -> Any?>()) } coAnswers {
            val block = secondArg<suspend () -> Any?>()
            block()
        }
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
            dispatcherProvider = dispatcherProvider
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
        Dispatchers.resetMain()
    }

    @Test
    fun `exportBackupJson successfully exports encrypted JSON containing cipher metadata and protecting plaintext`() = runTest(testDispatcher) {
        val sampleCategory = CategoryEntity(
            id = "cat-1",
            name = "Alimentação",
            colorHex = "#FF5722",
            iconName = "Restaurant"
        )
        coEvery { categoryDao.getAllList() } returns listOf(sampleCategory)
        coEvery { expenseItemDao.getAllList() } returns emptyList()
        coEvery { creditCardDao.getAllCardsList() } returns emptyList()

        val password = "SenhaDeBackupForte@123"
        val result = repository.exportBackupJson(password)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        val json = result.getOrThrow()
        assertTrue("Deve conter identificador de envelope criptografado", json.contains("PLATFORM_ENCRYPTED_BACKUP"))
        assertTrue("Deve conter versão 2", json.contains("\"version\": 2"))
        assertTrue("Deve conter algoritmo AES/GCM", json.contains("AES/GCM/NoPadding"))
        assertTrue("Deve conter KDF PBKDF2", json.contains("PBKDF2WithHmacSHA256"))
        assertTrue("Deve conter 600.000 iterações", json.contains("600000"))
        assertFalse("Não deve expor entidade em texto claro", json.contains("Alimentação"))
    }

    @Test
    fun `exportBackupJson with password shorter than 8 characters fails`() = runTest(testDispatcher) {
        val result = repository.exportBackupJson("1234567")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `restoreBackupFromJson with correct password successfully decrypts and restores entities`() = runTest(testDispatcher) {
        val sampleCategory = CategoryEntity(
            id = "cat-1",
            name = "Alimentação",
            colorHex = "#FF5722",
            iconName = "Restaurant"
        )
        coEvery { categoryDao.getAllList() } returns listOf(sampleCategory)
        coEvery { expenseItemDao.getAllList() } returns emptyList()
        coEvery { creditCardDao.getAllCardsList() } returns emptyList()

        val password = "SenhaDeBackupForte@123"
        val exportResult = repository.exportBackupJson(password)
        testDispatcher.scheduler.advanceUntilIdle()
        val encryptedJson = exportResult.getOrThrow()

        val restoreResult = repository.restoreBackupFromJson(encryptedJson, password)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(restoreResult.isSuccess)
    }

    @Test
    fun `restoreBackupFromJson with legacy v1 backup successfully restores entities`() = runTest(testDispatcher) {
        val legacyPassword = "MinhaSenhaV1@2026"
        val sampleDto = BackupDataDto(
            version = BackupDataDto.CURRENT_VERSION,
            exportedAt = System.currentTimeMillis(),
            categories = listOf(
                CategoryEntity(
                    id = "cat-legado",
                    name = "Legado V1",
                    colorHex = "#00FF00",
                    iconName = "History"
                )
            ),
            expenseItems = emptyList(),
            creditCards = emptyList(),
            creditCardInvoices = emptyList(),
            financialAccounts = emptyList(),
            paymentMethods = emptyList(),
            contacts = emptyList(),
            bills = emptyList(),
            installments = emptyList(),
            budgets = emptyList(),
            goals = emptyList(),
            goalContributions = emptyList()
        )
        val plaintextJson = gson.toJson(sampleDto)

        // Simula criação do backup no formato v1 legado (sem AAD, 65536 iterações)
        val random = java.security.SecureRandom()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) }
        val pbeSpec = javax.crypto.spec.PBEKeySpec(legacyPassword.toCharArray(), salt, 65536, 256)
        val key = javax.crypto.spec.SecretKeySpec(
            javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeSpec).encoded,
            "AES"
        )
        pbeSpec.clearPassword()
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, javax.crypto.spec.GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(plaintextJson.toByteArray(Charsets.UTF_8))

        val legacyDto = EncryptedBackupDto(
            format = EncryptedBackupDto.FORMAT_NAME,
            version = 1,
            algorithm = EncryptedBackupDto.ALGORITHM_NAME,
            kdf = EncryptedBackupDto.KDF_NAME,
            iterations = 65536,
            saltBase64 = java.util.Base64.getEncoder().encodeToString(salt),
            ivBase64 = java.util.Base64.getEncoder().encodeToString(iv),
            ciphertextBase64 = java.util.Base64.getEncoder().encodeToString(ciphertext),
            createdAt = System.currentTimeMillis()
        )
        val legacyJson = gson.toJson(legacyDto)

        val restoreResult = repository.restoreBackupFromJson(legacyJson, legacyPassword)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(restoreResult.isSuccess)
    }

    @Test
    fun `restoreBackupFromJson with wrong password fails and does not restore any data`() = runTest(testDispatcher) {
        val sampleCategory = CategoryEntity(
            id = "cat-1",
            name = "Alimentação",
            colorHex = "#FF5722",
            iconName = "Restaurant"
        )
        coEvery { categoryDao.getAllList() } returns listOf(sampleCategory)
        coEvery { expenseItemDao.getAllList() } returns emptyList()
        coEvery { creditCardDao.getAllCardsList() } returns emptyList()

        val password = "SenhaDeBackupForte@123"
        val exportResult = repository.exportBackupJson(password)
        testDispatcher.scheduler.advanceUntilIdle()
        val encryptedJson = exportResult.getOrThrow()

        val restoreResult = repository.restoreBackupFromJson(encryptedJson, "SenhaErrada@999")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(restoreResult.isFailure)
    }

    @Test
    fun `restoreBackupFromJson returns failure when json is corrupt or empty`() = runTest(testDispatcher) {
        val result = repository.restoreBackupFromJson("", "QualquerSenha123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isFailure)
    }
}
