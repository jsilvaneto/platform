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
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.GoalDao
import com.platform.app.data.local.dao.PaymentMethodDao
import com.platform.app.data.local.dao.SubcategoryDao
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.BudgetEntity
import com.platform.app.data.local.entity.CategoryEntity
import com.platform.app.data.local.entity.ContactEntity
import com.platform.app.data.local.entity.FinancialAccountEntity
import com.platform.app.data.local.entity.GoalEntity
import com.platform.app.data.local.entity.PaymentMethodEntity
import com.platform.app.data.local.entity.SubcategoryEntity

@Database(
    entities = [
        CategoryEntity::class,
        SubcategoryEntity::class,
        ContactEntity::class,
        FinancialAccountEntity::class,
        PaymentMethodEntity::class,
        BillEntity::class,
        BillInstallmentEntity::class,
        GoalEntity::class,
        BudgetEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class PlatformDatabase : RoomDatabase() {
    abstract val categoryDao: CategoryDao
    abstract val subcategoryDao: SubcategoryDao
    abstract val contactDao: ContactDao
    abstract val financialAccountDao: FinancialAccountDao
    abstract val paymentMethodDao: PaymentMethodDao
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
    }
}

