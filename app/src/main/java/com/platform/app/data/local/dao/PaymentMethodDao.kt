package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.platform.app.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods WHERE deletedAt IS NULL ORDER BY name ASC")
    fun getAll(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT COUNT(*) FROM payment_methods WHERE deletedAt IS NULL")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(method: PaymentMethodEntity)

    @Update
    suspend fun update(method: PaymentMethodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(method: PaymentMethodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(methods: List<PaymentMethodEntity>)

    @Query("UPDATE payment_methods SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun deleteById(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM payment_methods WHERE deletedAt IS NULL")
    suspend fun getAllList(): List<PaymentMethodEntity>

    @Query("DELETE FROM payment_methods WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeleted(olderThanMillis: Long)

    @Query("DELETE FROM payment_methods")
    suspend fun deleteAll()
}
