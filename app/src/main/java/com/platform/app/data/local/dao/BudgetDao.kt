package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.platform.app.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Query("UPDATE budgets SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun deleteById(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE budgets SET categoryName = :categoryName, colorHex = :colorHex, updatedAt = :updatedAt WHERE categoryId = :categoryId AND deletedAt IS NULL")
    suspend fun updateCategoryInfo(categoryId: String, categoryName: String, colorHex: String, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Query("SELECT * FROM budgets WHERE deletedAt IS NULL")
    suspend fun getAllList(): List<BudgetEntity>

    @Query("DELETE FROM budgets WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeleted(olderThanMillis: Long)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}
