package com.platform.app.data.repository

import androidx.room.withTransaction
import com.google.gson.GsonBuilder
import com.platform.app.core.dispatcher.DispatcherProvider
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.backup.BackupDataDto
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
import kotlinx.coroutines.flow.first
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

    override suspend fun exportBackupJson(): Result<String> = withContext(dispatcherProvider.io) {
        runCatching {
            val dto = BackupDataDto(
                version = BackupDataDto.CURRENT_VERSION,
                exportedAt = System.currentTimeMillis(),
                categories = categoryDao.getAllList(),
                expenseItems = expenseItemDao.getAll().first(),
                creditCards = creditCardDao.getAllCards().first(),
                financialAccounts = financialAccountDao.getAllList(),
                paymentMethods = paymentMethodDao.getAllList(),
                contacts = contactDao.getAllList(),
                bills = billDao.getAllList(),
                installments = billInstallmentDao.getAllInstallmentsList(),
                budgets = budgetDao.getAllList(),
                goals = goalDao.getAllList()
            )
            gson.toJson(dto)
        }
    }

    override suspend fun restoreBackupFromJson(jsonString: String): Result<Unit> = withContext(dispatcherProvider.io) {
        runCatching {
            val payload = gson.fromJson(jsonString, BackupDataDto::class.java)
                ?: throw IllegalArgumentException("Arquivo de backup inválido ou vazio.")

            if (payload.version <= 0) {
                throw IllegalArgumentException("Versão de backup inválida ou corrompida.")
            }

            database.withTransaction {
                // Limpeza ordenada respeitando constraints de chaves estrangeiras
                billInstallmentDao.deleteAll()
                billDao.deleteAll()
                budgetDao.deleteAll()
                goalDao.deleteAll()
                contactDao.deleteAll()
                financialAccountDao.deleteAll()
                paymentMethodDao.deleteAll()
                categoryDao.deleteAll()

                // Restauração ordenada
                if (payload.categories.isNotEmpty()) {
                    categoryDao.insertAll(payload.categories)
                }
                if (payload.expenseItems.isNotEmpty()) {
                    expenseItemDao.insertAll(payload.expenseItems)
                }
                if (payload.creditCards.isNotEmpty()) {
                    payload.creditCards.forEach { creditCardDao.insertCard(it) }
                }
                if (payload.creditCardInvoices.isNotEmpty()) {
                    payload.creditCardInvoices.forEach { creditCardDao.insertInvoice(it) }
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
            }
        }
    }
}
