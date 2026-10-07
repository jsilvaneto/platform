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
}
