package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.platform.app.data.local.entity.FinancialAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialAccountDao {
    @Query("SELECT * FROM financial_accounts WHERE deletedAt IS NULL ORDER BY name ASC")
    fun getAll(): Flow<List<FinancialAccountEntity>>

    @Query("SELECT * FROM financial_accounts WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getById(id: String): FinancialAccountEntity?

    @Query("SELECT COUNT(*) FROM financial_accounts WHERE deletedAt IS NULL")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(account: FinancialAccountEntity)

    @Update
    suspend fun update(account: FinancialAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: FinancialAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<FinancialAccountEntity>)

    @Query("UPDATE financial_accounts SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun deleteById(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM financial_accounts WHERE deletedAt IS NULL")
    suspend fun getAllList(): List<FinancialAccountEntity>

    @Query("DELETE FROM financial_accounts WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeleted(olderThanMillis: Long)

    @Query("DELETE FROM financial_accounts")
    suspend fun deleteAll()
}
