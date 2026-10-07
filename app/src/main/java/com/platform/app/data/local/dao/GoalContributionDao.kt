package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.platform.app.data.local.entity.GoalContributionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalContributionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contribution: GoalContributionEntity)

    @Query("SELECT * FROM goal_contributions WHERE goalId = :goalId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getByGoalForPeriod(goalId: String, startDate: Long, endDate: Long): Flow<List<GoalContributionEntity>>

    @Query("SELECT * FROM goal_contributions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getForPeriod(startDate: Long, endDate: Long): Flow<List<GoalContributionEntity>>

    @Query("SELECT * FROM goal_contributions WHERE goalId = :goalId ORDER BY date DESC")
    fun getByGoal(goalId: String): Flow<List<GoalContributionEntity>>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM goal_contributions WHERE goalId = :goalId")
    suspend fun sumByGoal(goalId: String): Long

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM goal_contributions WHERE date >= :startDate AND date <= :endDate")
    fun sumForPeriod(startDate: Long, endDate: Long): Flow<Long>

    @Query("SELECT * FROM goal_contributions ORDER BY date DESC")
    suspend fun getAll(): List<GoalContributionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contributions: List<GoalContributionEntity>)

    @Query("DELETE FROM goal_contributions WHERE goalId = :goalId")
    suspend fun deleteByGoalId(goalId: String)

    @Query("DELETE FROM goal_contributions")
    suspend fun deleteAll()
}
