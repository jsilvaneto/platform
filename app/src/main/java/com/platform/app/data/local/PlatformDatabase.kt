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
        BudgetEntity::class
    ],
    version = 6,
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
    }
}
