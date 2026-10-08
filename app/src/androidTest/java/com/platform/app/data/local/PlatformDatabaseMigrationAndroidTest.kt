package com.platform.app.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlatformDatabaseMigrationAndroidTest {

    private val TEST_DB = "migration-test-db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PlatformDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(TEST_DB)
    }

    @Test
    fun migrationFrom9To15_preservesInvoicesAndInstallmentsAndPassesForeignKeyCheck() {
        // 1. Criar banco na versão 9
        var db: SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 9)

        // 2. Popular banco v9 com dados relacionados (faturas ligadas a parcelas e cartoes)
        db.execSQL("""
            INSERT INTO categories (id, name, colorHex, iconName, nature, syncStatus)
            VALUES ('cat_1', 'Alimentação', '#FF0000', 'ic_food', 'NECESSARIO', 'PENDENTE')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO expense_items (id, name, categoryId, syncStatus)
            VALUES ('item_1', 'Supermercado', 'cat_1', 'PENDENTE')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO credit_cards (id, name, totalLimitCents, closingDay, dueDay, colorHex, syncStatus)
            VALUES ('card_1', 'Nubank Ultravioleta', 500000, 1, 10, '#8A05BE', 'PENDENTE')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO credit_card_invoices (id, creditCardId, referenceMonth, closingDate, dueDate, status, syncStatus)
            VALUES ('inv_1', 'card_1', '2026-10', 1727740800000, 1728518400000, 'ABERTA', 'PENDENTE')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO contacts (id, name, phone, email, street, number, complement, neighborhood, city, state, country, zipCode, createdAt)
            VALUES ('cont_1', 'Mercado Central', '', '', '', '', '', '', '', '', '', '', 1727740800000)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO financial_accounts (id, name, accountType, colorHex)
            VALUES ('acc_1', 'Conta Corrente', 'CHECKING', '#00FF00')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO payment_methods (id, name, iconName)
            VALUES ('pm_1', 'Cartão de Crédito', 'ic_card')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO bills (id, title, description, type, totalAmountCents, categoryId, itemId, invoiceId, contactId, financialAccountId, paymentMethodId, totalInstallments, recurrenceFrequency, recurrenceEndType, recurrenceEndDate, isPaused, createdAt)
            VALUES ('bill_1', 'Compras Mensais', 'Supermercado', 'INSTALLMENT', 20000, 'cat_1', 'item_1', 'inv_1', 'cont_1', 'acc_1', 'pm_1', 2, NULL, NULL, NULL, 0, 1727740800000)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO bill_installments (id, billId, installmentNumber, totalInstallments, amountCents, dueDate, paidAt, status, itemId, invoiceId, contactId, financialAccountId, paymentMethodId)
            VALUES ('inst_1', 'bill_1', 1, 2, 10000, 1728518400000, NULL, 'PENDING', 'item_1', 'inv_1', 'cont_1', 'acc_1', 'pm_1')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO bill_installments (id, billId, installmentNumber, totalInstallments, amountCents, dueDate, paidAt, status, itemId, invoiceId, contactId, financialAccountId, paymentMethodId)
            VALUES ('inst_2', 'bill_1', 2, 2, 10000, 1731196800000, NULL, 'PENDING', 'item_1', 'inv_1', 'cont_1', 'acc_1', 'pm_1')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO goals (id, name, targetAmountCents, currentAmountCents, deadlineDate, colorHex, createdAt)
            VALUES ('goal_1', 'Reserva de Emergência', 1000000, 250000, 1735689600000, '#0000FF', 1727740800000)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO budgets (id, categoryId, categoryName, limitAmountCents, colorHex, createdAt)
            VALUES ('bud_1', 'cat_1', 'Alimentação', 150000, '#FF0000', 1727740800000)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO transactions (id, description, amountCents, dueDate, paymentDate, status, categoryId, itemId, invoiceId, notes, isRecurring, syncStatus)
            VALUES ('tx_1', 'Transação Legada', 10000, 1728518400000, NULL, 'PENDENTE', 'cat_1', 'item_1', 'inv_1', NULL, 0, 'PENDENTE')
        """.trimIndent())

        // 3. Contagem antes das migrações
        val cardsCountBefore = getRowCount(db, "credit_cards")
        val invoicesCountBefore = getRowCount(db, "credit_card_invoices")
        val billsCountBefore = getRowCount(db, "bills")
        val installmentsCountBefore = getRowCount(db, "bill_installments")
        val categoriesCountBefore = getRowCount(db, "categories")
        val itemsCountBefore = getRowCount(db, "expense_items")

        assertEquals(1, cardsCountBefore)
        assertEquals(1, invoicesCountBefore)
        assertEquals(1, billsCountBefore)
        assertEquals(2, installmentsCountBefore)
        assertEquals(1, categoriesCountBefore)
        assertEquals(1, itemsCountBefore)

        db.close()

        // 4. Executar migrações encadeadas de 9 até 15
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB,
            15,
            true,
            PlatformDatabase.MIGRATION_9_10,
            PlatformDatabase.MIGRATION_10_11,
            PlatformDatabase.MIGRATION_11_12,
            PlatformDatabase.MIGRATION_12_13,
            PlatformDatabase.MIGRATION_13_14,
            PlatformDatabase.MIGRATION_14_15
        )

        // 5. Comparar contagem de linhas antes e depois
        val cardsCountAfter = getRowCount(migratedDb, "credit_cards")
        val invoicesCountAfter = getRowCount(migratedDb, "credit_card_invoices")
        val billsCountAfter = getRowCount(migratedDb, "bills")
        val installmentsCountAfter = getRowCount(migratedDb, "bill_installments")
        val categoriesCountAfter = getRowCount(migratedDb, "categories")
        val itemsCountAfter = getRowCount(migratedDb, "expense_items")

        assertEquals("Contagem de credit_cards deve ser preservada", cardsCountBefore, cardsCountAfter)
        assertEquals(
            "Contagem de credit_card_invoices deve ser preservada (sem deleção em cascata na migração 14->15)",
            invoicesCountBefore,
            invoicesCountAfter
        )
        assertEquals("Contagem de bills deve ser preservada", billsCountBefore, billsCountAfter)
        assertEquals("Contagem de bill_installments deve ser preservada", installmentsCountBefore, installmentsCountAfter)
        assertEquals("Contagem de categories deve ser preservada", categoriesCountBefore, categoriesCountAfter)
        assertEquals("Contagem de expense_items deve ser preservada", itemsCountBefore, itemsCountAfter)

        // 6. Verificar integridade da ligação das parcelas à fatura
        val cursorInstallments = migratedDb.query("SELECT id, invoiceId FROM bill_installments")
        var verifiedInstallmentCount = 0
        while (cursorInstallments.moveToNext()) {
            val invoiceId = cursorInstallments.getString(1)
            assertEquals("Parcela deve manter vínculo com a fatura inv_1", "inv_1", invoiceId)
            verifiedInstallmentCount++
        }
        cursorInstallments.close()
        assertEquals(2, verifiedInstallmentCount)

        // 7. Verificar criação e migração de goal_contributions na 10->11
        val contributionsCount = getRowCount(migratedDb, "goal_contributions")
        assertEquals("goal_contributions deve conter aporte inicial migrado da meta com saldo", 1, contributionsCount)

        // 8. Verificar normalização de conta na 12->13
        val accountCursor = migratedDb.query("SELECT accountType FROM financial_accounts WHERE id = 'acc_1'")
        assertTrue(accountCursor.moveToFirst())
        assertEquals("accountType CHECKING deve ter sido normalizado para CORRENTE", "CORRENTE", accountCursor.getString(0))
        accountCursor.close()

        // 9. Verificar adição de coluna type em contacts na 11->12
        val contactCursor = migratedDb.query("SELECT type FROM contacts WHERE id = 'cont_1'")
        assertTrue(contactCursor.moveToFirst())
        assertEquals("type padrão de contatos deve ser FORNECEDOR", "FORNECEDOR", contactCursor.getString(0))
        contactCursor.close()

        // 10. Executar PRAGMA foreign_key_check para atestar integridade referencial estrita
        val fkCursor = migratedDb.query("PRAGMA foreign_key_check")
        val fkViolations = mutableListOf<String>()
        while (fkCursor.moveToNext()) {
            val table = fkCursor.getString(0)
            val rowId = fkCursor.getLong(1)
            val parentTable = fkCursor.getString(2)
            val fkId = fkCursor.getInt(3)
            fkViolations.add("Violação FK em $table (rowid=$rowId), referenciando $parentTable (fkid=$fkId)")
        }
        fkCursor.close()

        assertTrue(
            "PRAGMA foreign_key_check deve retornar zero violações. Encontradas: $fkViolations",
            fkViolations.isEmpty()
        )

        migratedDb.close()
    }

    @Test
    fun migrationFrom9To17_completeChain_preservesAllDataAndPassesForeignKeyCheck() {
        var db: SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 9)

        db.execSQL("""
            INSERT INTO categories (id, name, colorHex, iconName, nature, syncStatus)
            VALUES ('cat_main', 'Moradia', '#0000FF', 'ic_home', 'NECESSARIO', 'PENDENTE')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO expense_items (id, name, categoryId, syncStatus)
            VALUES ('item_rent', 'Aluguel', 'cat_main', 'PENDENTE')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO credit_cards (id, name, totalLimitCents, closingDay, dueDay, colorHex, syncStatus)
            VALUES ('card_main', 'Mastercard Black', 1000000, 5, 15, '#000000', 'PENDENTE')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO credit_card_invoices (id, creditCardId, referenceMonth, closingDate, dueDate, status, syncStatus)
            VALUES ('inv_main', 'card_main', '2026-10', 1727740800000, 1728518400000, 'ABERTA', 'PENDENTE')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO contacts (id, name, phone, email, street, number, complement, neighborhood, city, state, country, zipCode, createdAt)
            VALUES ('cont_loc', 'Imobiliária', '', '', '', '', '', '', '', '', '', '', 1727740800000)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO financial_accounts (id, name, accountType, colorHex)
            VALUES ('acc_main', 'Principal', 'CORRENTE', '#10B981')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO payment_methods (id, name, iconName)
            VALUES ('pm_main', 'Boleto', 'ic_barcode')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO bills (id, title, description, type, totalAmountCents, categoryId, itemId, invoiceId, contactId, financialAccountId, paymentMethodId, totalInstallments, recurrenceFrequency, recurrenceEndType, recurrenceEndDate, isPaused, createdAt)
            VALUES ('bill_rec', 'Aluguel Mensal', 'Locação', 'RECURRING', 300000, 'cat_main', 'item_rent', 'inv_main', 'cont_loc', 'acc_main', 'pm_main', 1, 'MONTHLY', 'NEVER', NULL, 0, 1727740800000)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO bill_installments (id, billId, installmentNumber, totalInstallments, amountCents, dueDate, paidAt, status, itemId, invoiceId, contactId, financialAccountId, paymentMethodId)
            VALUES ('inst_rec_1', 'bill_rec', 1, 1, 300000, 1728518400000, NULL, 'PENDING', 'item_rent', 'inv_main', 'cont_loc', 'acc_main', 'pm_main')
        """.trimIndent())

        db.execSQL("""
            INSERT INTO goals (id, name, targetAmountCents, currentAmountCents, deadlineDate, colorHex, createdAt)
            VALUES ('goal_car', 'Carro Novo', 5000000, 0, 1735689600000, '#F59E0B', 1727740800000)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO budgets (id, categoryId, categoryName, limitAmountCents, colorHex, createdAt)
            VALUES ('bud_main', 'cat_main', 'Moradia', 350000, '#0000FF', 1727740800000)
        """.trimIndent())

        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB,
            17,
            true,
            PlatformDatabase.MIGRATION_9_10,
            PlatformDatabase.MIGRATION_10_11,
            PlatformDatabase.MIGRATION_11_12,
            PlatformDatabase.MIGRATION_12_13,
            PlatformDatabase.MIGRATION_13_14,
            PlatformDatabase.MIGRATION_14_15,
            PlatformDatabase.MIGRATION_15_16,
            PlatformDatabase.MIGRATION_16_17
        )

        assertEquals(1, getRowCount(migratedDb, "credit_cards"))
        assertEquals(1, getRowCount(migratedDb, "credit_card_invoices"))
        assertEquals(1, getRowCount(migratedDb, "bills"))
        assertEquals(1, getRowCount(migratedDb, "bill_installments"))

        val billCursor = migratedDb.query("SELECT recurrenceAnchorDate, creditCardId FROM bills WHERE id = 'bill_rec'")
        assertTrue(billCursor.moveToFirst())
        assertEquals(1728518400000L, billCursor.getLong(0))
        assertEquals("card_main", billCursor.getString(1))
        billCursor.close()

        val fkCursor = migratedDb.query("PRAGMA foreign_key_check")
        val fkViolations = mutableListOf<String>()
        while (fkCursor.moveToNext()) {
            val table = fkCursor.getString(0)
            val rowId = fkCursor.getLong(1)
            val parentTable = fkCursor.getString(2)
            val fkId = fkCursor.getInt(3)
            fkViolations.add("Violação FK em $table (rowid=$rowId), referenciando $parentTable (fkid=$fkId)")
        }
        fkCursor.close()

        assertTrue(
            "Cadeia completa 9->17 deve passar em PRAGMA foreign_key_check com 0 violações",
            fkViolations.isEmpty()
        )

        migratedDb.close()
    }

    private fun getRowCount(db: SupportSQLiteDatabase, table: String): Int {
        val cursor = db.query("SELECT COUNT(*) FROM $table")
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }
}
