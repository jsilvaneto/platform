package com.platform.app.data.repository

import androidx.room.withTransaction
import com.google.gson.GsonBuilder
import com.platform.app.core.dispatcher.DispatcherProvider
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.backup.BackupCryptoHelper
import com.platform.app.data.local.backup.BackupDataDto
import com.platform.app.data.local.backup.EncryptedBackupDto
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
import com.platform.app.domain.repository.BackupRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val database: PlatformDatabase,
    private val categoryDao: CategoryDao,
    private val expenseItemDao: ExpenseItemDao,
    private val creditCardDao: CreditCardDao,
    private val financialAccountDao: FinancialAccountDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val contactDao: ContactDao,
    private val billDao: BillDao,
    private val billInstallmentDao: BillInstallmentDao,
    private val goalDao: GoalDao,
    private val budgetDao: BudgetDao,
    private val dispatcherProvider: DispatcherProvider
) : BackupRepository {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override suspend fun exportBackupJson(password: String): Result<String> = withContext(dispatcherProvider.io) {
        runCatching {
            require(password.isNotBlank()) { "A senha ou PIN de backup não pode estar vazia." }
            val dto = BackupDataDto(
                version = BackupDataDto.CURRENT_VERSION,
                exportedAt = System.currentTimeMillis(),
                categories = categoryDao.getAllList(),
                expenseItems = expenseItemDao.getAllList(),
                creditCards = creditCardDao.getAllCardsList(),
                creditCardInvoices = creditCardDao.getAllInvoicesList(),
                financialAccounts = financialAccountDao.getAllList(),
                paymentMethods = paymentMethodDao.getAllList(),
                contacts = contactDao.getAllList(),
                bills = billDao.getAllList(),
                installments = billInstallmentDao.getAllInstallmentsList(),
                budgets = budgetDao.getAllList(),
                goals = goalDao.getAllList(),
                goalContributions = goalDao.getAllContributions()
            )
            val plaintextJson = gson.toJson(dto)
            val encryptedDto = BackupCryptoHelper.encrypt(plaintextJson, password)
            gson.toJson(encryptedDto)
        }
    }

    override suspend fun restoreBackupFromJson(encryptedBackupJson: String, password: String): Result<Unit> = withContext(dispatcherProvider.io) {
        runCatching {
            if (encryptedBackupJson.isBlank()) {
                throw IllegalArgumentException("Arquivo de backup inválido ou vazio.")
            }
            require(password.isNotBlank()) { "A senha ou PIN para restauração é obrigatória." }

            val encryptedDto = try {
                gson.fromJson(encryptedBackupJson, EncryptedBackupDto::class.java)
            } catch (e: Exception) {
                throw IllegalArgumentException("Arquivo de backup com formato inválido ou corrompido.", e)
            } ?: throw IllegalArgumentException("Arquivo de backup vazio.")

            val plaintextJson = BackupCryptoHelper.decrypt(encryptedDto, password)

            val payload = gson.fromJson(plaintextJson, BackupDataDto::class.java)
                ?: throw IllegalArgumentException("Dados de backup corrompidos.")

            if (payload.version <= 0) {
                throw IllegalArgumentException("Versão de backup inválida ou corrompida.")
            }

            database.withTransaction {
                // 1. Limpeza ordenada respeitando constraints de chaves estrangeiras (filhos primeiro)
                billInstallmentDao.deleteAll()
                billDao.deleteAll()
                expenseItemDao.deleteAll()
                creditCardDao.deleteAllInvoices()
                creditCardDao.deleteAllCards()
                categoryDao.deleteAll()
                budgetDao.deleteAll()
                goalDao.deleteAllContributions()
                goalDao.deleteAll()
                contactDao.deleteAll()
                financialAccountDao.deleteAll()
                paymentMethodDao.deleteAll()

                // 2. Restauração ordenada (pais primeiro, filhos depois)
                if (payload.categories.isNotEmpty()) {
                    categoryDao.insertAll(payload.categories)
                }
                if (payload.expenseItems.isNotEmpty()) {
                    expenseItemDao.insertAll(payload.expenseItems)
                }
                if (payload.creditCards.isNotEmpty()) {
                    creditCardDao.insertAllCards(payload.creditCards)
                }
                if (payload.creditCardInvoices.isNotEmpty()) {
                    creditCardDao.insertAllInvoices(payload.creditCardInvoices)
                }
                if (payload.financialAccounts.isNotEmpty()) {
                    financialAccountDao.insertAll(payload.financialAccounts)
                }
                if (payload.paymentMethods.isNotEmpty()) {
                    paymentMethodDao.insertAll(payload.paymentMethods)
                }
                if (payload.contacts.isNotEmpty()) {
                    contactDao.insertAll(payload.contacts)
                }
                if (payload.bills.isNotEmpty()) {
                    billDao.insertAll(payload.bills)
                }
                if (payload.installments.isNotEmpty()) {
                    billInstallmentDao.insertAll(payload.installments)
                }
                if (payload.budgets.isNotEmpty()) {
                    budgetDao.insertAll(payload.budgets)
                }
                if (payload.goals.isNotEmpty()) {
                    goalDao.insertAll(payload.goals)
                }
                if (payload.goalContributions.isNotEmpty()) {
                    goalDao.insertAllContributions(payload.goalContributions)
                }
            }
        }
    }
}
