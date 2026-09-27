package com.platform.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.ItemDao
import com.platform.app.data.local.dao.PaymentMethodDao
import com.platform.app.data.local.dao.SubcategoryDao
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.CategoryEntity
import com.platform.app.data.local.entity.ContactEntity
import com.platform.app.data.local.entity.FinancialAccountEntity
import com.platform.app.data.local.entity.ItemEntity
import com.platform.app.data.local.entity.PaymentMethodEntity
import com.platform.app.data.local.entity.SubcategoryEntity

@Database(
    entities = [
        ItemEntity::class,
        CategoryEntity::class,
        SubcategoryEntity::class,
        ContactEntity::class,
        FinancialAccountEntity::class,
        PaymentMethodEntity::class,
        BillEntity::class,
        BillInstallmentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PlatformDatabase : RoomDatabase() {
    abstract val itemDao: ItemDao
    abstract val categoryDao: CategoryDao
    abstract val subcategoryDao: SubcategoryDao
    abstract val contactDao: ContactDao
    abstract val financialAccountDao: FinancialAccountDao
    abstract val paymentMethodDao: PaymentMethodDao
    abstract val billDao: BillDao
    abstract val billInstallmentDao: BillInstallmentDao

    companion object {
        const val DATABASE_NAME = "platform_db"
    }
}

