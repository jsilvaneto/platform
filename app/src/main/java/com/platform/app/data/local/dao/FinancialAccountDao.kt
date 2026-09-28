package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.platform.app.data.local.entity.FinancialAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialAccountDao {
    @Query("SELECT * FROM financial_accounts ORDER BY name ASC")
    fun getAll(): Flow<List<FinancialAccountEntity>>

    @Query("SELECT * FROM financial_accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FinancialAccountEntity?

    @Query("SELECT COUNT(*) FROM financial_accounts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: FinancialAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<FinancialAccountEntity>)

    @Query("DELETE FROM financial_accounts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM financial_accounts")
    suspend fun getAllList(): List<FinancialAccountEntity>

    @Query("DELETE FROM financial_accounts")
    suspend fun deleteAll()
}
