package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.platform.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE deletedAt IS NULL ORDER BY name ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories WHERE deletedAt IS NULL")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("UPDATE categories SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun deleteById(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM categories WHERE deletedAt IS NULL")
    suspend fun getAllList(): List<CategoryEntity>

    @Query("DELETE FROM categories WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeleted(olderThanMillis: Long)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
