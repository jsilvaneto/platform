package com.platform.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.BudgetEntity
import com.platform.app.data.local.entity.CategoryEntity
import com.platform.app.data.local.entity.ContactEntity
import com.platform.app.data.local.entity.CreditCardEntity
import com.platform.app.data.local.entity.CreditCardInvoiceEntity
import com.platform.app.data.local.entity.ExpenseItemEntity
import com.platform.app.data.local.entity.FinancialAccountEntity
import com.platform.app.data.local.entity.GoalEntity
import com.platform.app.data.local.entity.PaymentMethodEntity
import com.platform.app.data.local.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        ExpenseItemEntity::class,
        ContactEntity::class,
        FinancialAccountEntity::class,
        PaymentMethodEntity::class,
        CreditCardEntity::class,
        CreditCardInvoiceEntity::class,
        BillEntity::class,
        BillInstallmentEntity::class,
        GoalEntity::class,
        BudgetEntity::class,
        TransactionEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class PlatformDatabase : RoomDatabase() {
    abstract val categoryDao: CategoryDao
    abstract val expenseItemDao: ExpenseItemDao
    abstract val contactDao: ContactDao
    abstract val financialAccountDao: FinancialAccountDao
    abstract val paymentMethodDao: PaymentMethodDao
    abstract val creditCardDao: CreditCardDao
    abstract val billDao: BillDao
    abstract val billInstallmentDao: BillInstallmentDao
    abstract val goalDao: GoalDao
    abstract val budgetDao: BudgetDao
    abstract val transactionDao: TransactionDao

    companion object {
        const val DATABASE_NAME = "platform_db"

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS items")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN nature TEXT NOT NULL DEFAULT 'NECESSARIO'")
                db.execSQL("DROP TABLE IF EXISTS subcategories")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS expense_items (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        categoryId TEXT NOT NULL,
                        syncStatus TEXT NOT NULL DEFAULT 'PENDENTE',
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE RESTRICT
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_items_categoryId ON expense_items(categoryId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS credit_cards (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        totalLimitCents INTEGER NOT NULL,
                        closingDay INTEGER NOT NULL,
                        dueDay INTEGER NOT NULL,
                        colorHex TEXT NOT NULL DEFAULT '#3B82F6',
                        syncStatus TEXT NOT NULL DEFAULT 'PENDENTE'
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS credit_card_invoices (
                        id TEXT NOT NULL PRIMARY KEY,
                        creditCardId TEXT NOT NULL,
                        referenceMonth TEXT NOT NULL,
                        closingDate INTEGER NOT NULL,
                        dueDate INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ABERTA',
                        syncStatus TEXT NOT NULL DEFAULT 'PENDENTE',
                        FOREIGN KEY(creditCardId) REFERENCES credit_cards(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_invoices_creditCardId ON credit_card_invoices(creditCardId)")

                db.execSQL("ALTER TABLE bills ADD COLUMN itemId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE bills ADD COLUMN invoiceId TEXT DEFAULT NULL")

                db.execSQL("ALTER TABLE bill_installments ADD COLUMN itemId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE bill_installments ADD COLUMN invoiceId TEXT DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bill_installments_itemId ON bill_installments(itemId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bill_installments_invoiceId ON bill_installments(invoiceId)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDENTE'")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        id TEXT NOT NULL PRIMARY KEY,
                        description TEXT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        dueDate INTEGER NOT NULL,
                        paymentDate INTEGER,
                        status TEXT NOT NULL DEFAULT 'PENDENTE',
                        categoryId TEXT,
                        itemId TEXT,
                        invoiceId TEXT,
                        notes TEXT,
                        isRecurring INTEGER NOT NULL DEFAULT 0,
                        syncStatus TEXT NOT NULL DEFAULT 'PENDENTE',
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL,
                        FOREIGN KEY(itemId) REFERENCES expense_items(id) ON DELETE SET NULL,
                        FOREIGN KEY(invoiceId) REFERENCES credit_card_invoices(id) ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions(categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_itemId ON transactions(itemId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_invoiceId ON transactions(invoiceId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_dueDate ON transactions(dueDate)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bills ADD COLUMN recurrenceFrequency TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE bills ADD COLUMN recurrenceEndType TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE bills ADD COLUMN recurrenceEndDate INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bills ADD COLUMN isPaused INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bill_installments ADD COLUMN actualPaymentDate INTEGER DEFAULT NULL")
            }
        }
    }
}
