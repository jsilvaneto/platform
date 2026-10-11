package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.platform.app.data.local.entity.GoalContributionEntity
import com.platform.app.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Query("UPDATE goals SET currentAmountCents = currentAmountCents + :amountCents, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun addContribution(id: String, amountCents: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE goals SET currentAmountCents = :totalCents, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updateCurrentAmount(id: String, totalCents: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE goals SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun deleteById(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<GoalEntity>)

    @Query("SELECT * FROM goals WHERE deletedAt IS NULL")
    suspend fun getAllList(): List<GoalEntity>

    @Query("DELETE FROM goals WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeletedGoals(olderThanMillis: Long)

    @Query("DELETE FROM goals")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: GoalContributionEntity)

    @Query("SELECT * FROM goal_contributions WHERE date >= :startDate AND date <= :endDate AND deletedAt IS NULL ORDER BY date DESC")
    fun getContributionsForPeriod(startDate: Long, endDate: Long): Flow<List<GoalContributionEntity>>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM goal_contributions WHERE date >= :startDate AND date <= :endDate AND deletedAt IS NULL")
    fun getMonthlyContributionSum(startDate: Long, endDate: Long): Flow<Long>

    @Query("SELECT * FROM goal_contributions WHERE deletedAt IS NULL")
    suspend fun getAllContributions(): List<GoalContributionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContributions(contributions: List<GoalContributionEntity>)

    @Query("DELETE FROM goal_contributions WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeletedContributions(olderThanMillis: Long)

    @Query("DELETE FROM goal_contributions")
    suspend fun deleteAllContributions()
}
