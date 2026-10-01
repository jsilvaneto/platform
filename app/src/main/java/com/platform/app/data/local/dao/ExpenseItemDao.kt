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
    @Query("SELECT * FROM expense_items ORDER BY name ASC")
    fun getAll(): Flow<List<ExpenseItemEntity>>

    @Query("SELECT * FROM expense_items WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getByCategoryId(categoryId: String): Flow<List<ExpenseItemEntity>>

    @Query("SELECT * FROM expense_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExpenseItemEntity?

    @Upsert
    suspend fun upsert(item: ExpenseItemEntity)

    @Update
    suspend fun update(item: ExpenseItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ExpenseItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ExpenseItemEntity>)

    @Query("DELETE FROM expense_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM expense_items")
    suspend fun count(): Int
}
