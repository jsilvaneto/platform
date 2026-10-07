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
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: String): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Query("UPDATE goals SET currentAmountCents = currentAmountCents + :amountCents WHERE id = :id")
    suspend fun addContribution(id: String, amountCents: Long)

    @Query("UPDATE goals SET currentAmountCents = :totalCents WHERE id = :id")
    suspend fun updateCurrentAmount(id: String, totalCents: Long)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<GoalEntity>)

    @Query("SELECT * FROM goals")
    suspend fun getAllList(): List<GoalEntity>

    @Query("DELETE FROM goals")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: GoalContributionEntity)

    @Query("SELECT * FROM goal_contributions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getContributionsForPeriod(startDate: Long, endDate: Long): Flow<List<GoalContributionEntity>>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM goal_contributions WHERE date >= :startDate AND date <= :endDate")
    fun getMonthlyContributionSum(startDate: Long, endDate: Long): Flow<Long>

    @Query("SELECT * FROM goal_contributions")
    suspend fun getAllContributions(): List<GoalContributionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContributions(contributions: List<GoalContributionEntity>)

    @Query("DELETE FROM goal_contributions")
    suspend fun deleteAllContributions()
}
