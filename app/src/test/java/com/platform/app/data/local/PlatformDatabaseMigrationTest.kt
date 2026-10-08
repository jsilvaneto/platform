package com.platform.app.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class PlatformDatabaseMigrationTest {

    @Test
    fun `migration 13 to 14 should drop transactions table`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)

        assertEquals(13, PlatformDatabase.MIGRATION_13_14.startVersion)
        assertEquals(14, PlatformDatabase.MIGRATION_13_14.endVersion)

        PlatformDatabase.MIGRATION_13_14.migrate(db)

        verify(exactly = 1) {
            db.execSQL("DROP TABLE IF EXISTS transactions")
        }
    }

    @Test
    fun `migration 14 to 15 should remove syncStatus from tables`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)

        assertEquals(14, PlatformDatabase.MIGRATION_14_15.startVersion)
        assertEquals(15, PlatformDatabase.MIGRATION_14_15.endVersion)

        PlatformDatabase.MIGRATION_14_15.migrate(db)

        verify(atLeast = 1) {
            db.execSQL("PRAGMA foreign_keys = OFF")
            db.execSQL(match { it.contains("categories_new") })
            db.execSQL(match { it.contains("DROP TABLE categories") })
            db.execSQL(match { it.contains("ALTER TABLE categories_new RENAME TO categories") })
            db.execSQL(match { it.contains("expense_items_new") })
            db.execSQL(match { it.contains("DROP TABLE expense_items") })
            db.execSQL(match { it.contains("credit_cards_new") })
            db.execSQL(match { it.contains("DROP TABLE credit_cards") })
            db.execSQL(match { it.contains("credit_card_invoices_new") })
            db.execSQL(match { it.contains("DROP TABLE credit_card_invoices") })
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }

    @Test
    fun `migration 15 to 16 should add recurrenceAnchorDate and creditCardId to bills table`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)

        assertEquals(15, PlatformDatabase.MIGRATION_15_16.startVersion)
        assertEquals(16, PlatformDatabase.MIGRATION_15_16.endVersion)

        PlatformDatabase.MIGRATION_15_16.migrate(db)

        verify(atLeast = 1) {
            db.execSQL("ALTER TABLE bills ADD COLUMN recurrenceAnchorDate INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE bills ADD COLUMN creditCardId TEXT DEFAULT NULL")
            db.execSQL(match { it.contains("UPDATE bills") && it.contains("recurrenceAnchorDate") })
            db.execSQL(match { it.contains("UPDATE bills") && it.contains("creditCardId") })
        }
    }
}
