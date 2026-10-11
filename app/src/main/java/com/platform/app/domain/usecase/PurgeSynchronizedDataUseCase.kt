package com.platform.app.domain.usecase

import androidx.room.withTransaction
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.BudgetDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.dao.ExpenseItemDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.GoalContributionDao
import com.platform.app.data.local.dao.GoalDao
import com.platform.app.data.local.dao.PaymentDao
import com.platform.app.data.local.dao.PaymentMethodDao
import com.platform.app.data.local.dao.SyncDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caso de Uso para Purga Definitiva de Registros Sincronizados (Prompt 65).
 * Remove do banco de dados local registros marcados com soft delete (deletedAt != null)
 * anteriores a uma janela segura (ex: 30 dias) ou após confirmação de sincronização pelo backend.
 */
@Singleton
class PurgeSynchronizedDataUseCase @Inject constructor(
    private val database: PlatformDatabase,
    private val billDao: BillDao,
    private val installmentDao: BillInstallmentDao,
    private val creditCardDao: CreditCardDao,
    private val categoryDao: CategoryDao,
    private val expenseItemDao: ExpenseItemDao,
    private val contactDao: ContactDao,
    private val financialAccountDao: FinancialAccountDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val goalDao: GoalDao,
    private val goalContributionDao: GoalContributionDao,
    private val budgetDao: BudgetDao,
    private val paymentDao: PaymentDao,
    private val syncDao: SyncDao
) {

    /**
     * Executa a purga atômica de todas as entidades marcadas para deleção anteriores a [olderThanMillis].
     * Também expurga entradas de outbox com status 'SYNCED'.
     */
    suspend operator fun invoke(olderThanMillis: Long = System.currentTimeMillis() - DEFAULT_RETENTION_MILLIS) {
        database.withTransaction {
            billDao.purgeDeleted(olderThanMillis)
            installmentDao.purgeDeleted(olderThanMillis)
            creditCardDao.purgeDeletedCards(olderThanMillis)
            creditCardDao.purgeDeletedInvoices(olderThanMillis)
            categoryDao.purgeDeleted(olderThanMillis)
            expenseItemDao.purgeDeleted(olderThanMillis)
            contactDao.purgeDeleted(olderThanMillis)
            financialAccountDao.purgeDeleted(olderThanMillis)
            paymentMethodDao.purgeDeleted(olderThanMillis)
            goalDao.purgeDeletedGoals(olderThanMillis)
            goalContributionDao.purgeDeleted(olderThanMillis)
            budgetDao.purgeDeleted(olderThanMillis)
            paymentDao.purgeDeleted(olderThanMillis)
            syncDao.purgeSyncedEntries()
        }
    }

    companion object {
        // Retenção padrão: 30 dias após o soft-delete para garantir sincronização bidirecional
        const val DEFAULT_RETENTION_MILLIS = 30L * 24L * 60L * 60L * 1000L
    }
}
