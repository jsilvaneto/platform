package com.platform.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.platform.app.data.local.dao.ItemDao
import com.platform.app.data.local.entity.ItemEntity

@Database(
    entities = [ItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PlatformDatabase : RoomDatabase() {
    abstract val itemDao: ItemDao

    companion object {
        const val DATABASE_NAME = "platform_db"
    }
}
