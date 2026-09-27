package com.platform.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ItemDao
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.CategoryEntity
import com.platform.app.data.local.entity.ItemEntity

@Database(
    entities = [
        ItemEntity::class,
        CategoryEntity::class,
        BillEntity::class,
        BillInstallmentEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PlatformDatabase : RoomDatabase() {
    abstract val itemDao: ItemDao
    abstract val categoryDao: CategoryDao
    abstract val billDao: BillDao
    abstract val billInstallmentDao: BillInstallmentDao

    companion object {
        const val DATABASE_NAME = "platform_db"
    }
}
