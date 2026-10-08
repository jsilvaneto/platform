package com.platform.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.platform.app.data.local.converter.FinancialAccountTypeConverter
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.BudgetDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.dao.ExpenseItemDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.GoalDao
import com.platform.app.data.local.dao.GoalContributionDao
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
import com.platform.app.data.local.entity.GoalContributionEntity
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
        GoalContributionEntity::class,
        BudgetEntity::class
    ],
    version = 17,
    exportSchema = true
)
@TypeConverters(FinancialAccountTypeConverter::class)
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
    abstract val goalContributionDao: GoalContributionDao
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

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS goal_contributions (
                        id TEXT NOT NULL PRIMARY KEY,
                        goalId TEXT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        FOREIGN KEY(goalId) REFERENCES goals(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goal_contributions_goalId ON goal_contributions(goalId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goal_contributions_date ON goal_contributions(date)")
                db.execSQL("""
                    INSERT INTO goal_contributions (id, goalId, amountCents, date)
                    SELECT id || '_init', id, currentAmountCents, createdAt FROM goals WHERE currentAmountCents > 0
                """.trimIndent())
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE contacts ADD COLUMN type TEXT NOT NULL DEFAULT 'FORNECEDOR'")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE financial_accounts SET accountType = 'CORRENTE' WHERE accountType IN ('CHECKING', 'Conta Corrente', 'Corrente', 'CREDIT_CARD', 'Cartão de Crédito', 'Outro')")
                db.execSQL("UPDATE financial_accounts SET accountType = 'CARTEIRA' WHERE accountType IN ('CASH', 'Dinheiro / Carteira', 'Carteira', 'Dinheiro')")
                db.execSQL("UPDATE financial_accounts SET accountType = 'POUPANCA' WHERE accountType IN ('SAVINGS', 'Poupança', 'Poupanca')")
                db.execSQL("UPDATE financial_accounts SET accountType = 'INVESTIMENTO' WHERE accountType IN ('INVESTMENT', 'Investimento / Reserva', 'Investimento', 'Reserva de Emergência')")
                db.execSQL("UPDATE financial_accounts SET accountType = 'CORRENTE' WHERE accountType NOT IN ('CORRENTE', 'CARTEIRA', 'POUPANCA', 'INVESTIMENTO')")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS transactions")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ativa o diferimento de verificacao de chaves estrangeiras durante a transacao da migracao
                db.execSQL("PRAGMA defer_foreign_keys = ON")

                // 1. categories: preparar categories_new
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        iconName TEXT NOT NULL,
                        nature TEXT NOT NULL DEFAULT 'NECESSARIO'
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO categories_new (id, name, colorHex, iconName, nature)
                    SELECT id, name, colorHex, iconName, nature FROM categories
                """.trimIndent())

                // 2. expense_items: preparar expense_items_new (filha de categories)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS expense_items_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        categoryId TEXT NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE RESTRICT
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO expense_items_new (id, name, categoryId)
                    SELECT id, name, categoryId FROM expense_items
                """.trimIndent())

                // 3. credit_card_invoices: preparar credit_card_invoices_new (filha de credit_cards) ANTES de dropar o pai
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS credit_card_invoices_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        creditCardId TEXT NOT NULL,
                        referenceMonth TEXT NOT NULL,
                        closingDate INTEGER NOT NULL,
                        dueDate INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ABERTA',
                        FOREIGN KEY(creditCardId) REFERENCES credit_cards(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO credit_card_invoices_new (id, creditCardId, referenceMonth, closingDate, dueDate, status)
                    SELECT id, creditCardId, referenceMonth, closingDate, dueDate, status FROM credit_card_invoices
                """.trimIndent())

                // 4. credit_cards: preparar credit_cards_new
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS credit_cards_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        totalLimitCents INTEGER NOT NULL,
                        closingDay INTEGER NOT NULL,
                        dueDay INTEGER NOT NULL,
                        colorHex TEXT NOT NULL DEFAULT '#3B82F6'
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO credit_cards_new (id, name, totalLimitCents, closingDay, dueDay, colorHex)
                    SELECT id, name, totalLimitCents, closingDay, dueDay, colorHex FROM credit_cards
                """.trimIndent())

                // Dropar e renomear credit_cards e credit_card_invoices (filha dropada antes do pai)
                db.execSQL("DROP TABLE credit_card_invoices")
                db.execSQL("DROP TABLE credit_cards")
                db.execSQL("ALTER TABLE credit_cards_new RENAME TO credit_cards")
                db.execSQL("ALTER TABLE credit_card_invoices_new RENAME TO credit_card_invoices")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_invoices_creditCardId ON credit_card_invoices(creditCardId)")

                // Dropar e renomear categories e expense_items (filha dropada antes da mae)
                db.execSQL("DROP TABLE expense_items")
                db.execSQL("DROP TABLE categories")
                db.execSQL("ALTER TABLE categories_new RENAME TO categories")
                db.execSQL("ALTER TABLE expense_items_new RENAME TO expense_items")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_items_categoryId ON expense_items(categoryId)")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bills ADD COLUMN recurrenceAnchorDate INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE bills ADD COLUMN creditCardId TEXT DEFAULT NULL")
                db.execSQL("""
                    UPDATE bills
                    SET recurrenceAnchorDate = (
                        SELECT MIN(dueDate) FROM bill_installments WHERE bill_installments.billId = bills.id
                    )
                    WHERE type = 'RECURRING'
                """.trimIndent())
                db.execSQL("""
                    UPDATE bills
                    SET creditCardId = (
                        SELECT creditCardId FROM credit_card_invoices WHERE credit_card_invoices.id = bills.invoiceId
                    )
                    WHERE invoiceId IS NOT NULL
                """.trimIndent())
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Remover duplicatas existentes: manter a mais antiga por (billId, installmentNumber), preservando as ja pagas
                db.execSQL("""
                    DELETE FROM bill_installments 
                    WHERE rowid IN (
                        SELECT b.rowid
                        FROM bill_installments b
                        JOIN bill_installments a 
                          ON a.billId = b.billId 
                         AND a.installmentNumber = b.installmentNumber
                         AND (
                             ((a.status = 'PAID' OR a.paidAt IS NOT NULL) AND (b.status != 'PAID' AND b.paidAt IS NULL))
                             OR
                             ((a.status = 'PAID' OR a.paidAt IS NOT NULL) AND (b.status = 'PAID' OR b.paidAt IS NOT NULL) AND a.rowid < b.rowid)
                             OR
                             ((a.status != 'PAID' AND a.paidAt IS NULL) AND (b.status != 'PAID' AND b.paidAt IS NULL) AND a.rowid < b.rowid)
                         )
                    )
                """.trimIndent())

                // 2. Atualizar contagem total de parcelas de contas recorrentes
                db.execSQL("""
                    UPDATE bills 
                    SET totalInstallments = (
                        SELECT COUNT(*) 
                        FROM bill_installments 
                        WHERE bill_installments.billId = bills.id
                    )
                    WHERE type = 'RECURRING'
                """.trimIndent())

                // 3. Criar UNIQUE INDEX (billId, installmentNumber)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_bill_installments_billId_installmentNumber ON bill_installments(billId, installmentNumber)")
            }
        }
    }
}
