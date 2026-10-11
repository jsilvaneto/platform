package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.platform.app.data.local.entity.ExpenseItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseItemDao {
    @Query("SELECT * FROM expense_items WHERE deletedAt IS NULL ORDER BY name ASC")
    fun getAll(): Flow<List<ExpenseItemEntity>>

    @Query("SELECT * FROM expense_items WHERE categoryId = :categoryId AND deletedAt IS NULL ORDER BY name ASC")
    fun getByCategoryId(categoryId: String): Flow<List<ExpenseItemEntity>>

    @Query("SELECT * FROM expense_items WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getById(id: String): ExpenseItemEntity?

    @Upsert
    suspend fun upsert(item: ExpenseItemEntity)

    @Update
    suspend fun update(item: ExpenseItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ExpenseItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ExpenseItemEntity>)

    @Query("UPDATE expense_items SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun deleteById(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM expense_items WHERE deletedAt IS NULL")
    suspend fun count(): Int

    @Query("SELECT * FROM expense_items WHERE deletedAt IS NULL")
    suspend fun getAllList(): List<ExpenseItemEntity>

    @Query("DELETE FROM expense_items WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeleted(olderThanMillis: Long)

    @Query("DELETE FROM expense_items")
    suspend fun deleteAll()
}
