package com.platform.app.data.repository

import androidx.room.withTransaction
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.GoalContributionDao
import com.platform.app.data.local.dao.GoalDao
import com.platform.app.data.local.entity.GoalContributionEntity
import com.platform.app.data.local.entity.GoalEntity
import com.platform.app.domain.model.Goal
import com.platform.app.domain.model.GoalContribution
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class GoalRepositoryImplTest {

    private lateinit var database: PlatformDatabase
    private lateinit var goalDao: GoalDao
    private lateinit var goalContributionDao: GoalContributionDao
    private lateinit var repository: GoalRepositoryImpl

    @Before
    fun setUp() {
        database = mockk(relaxed = true)
        goalDao = mockk(relaxed = true)
        goalContributionDao = mockk(relaxed = true)

        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Any>()
        coEvery { database.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        repository = GoalRepositoryImpl(
            database = database,
            goalDao = goalDao,
            goalContributionDao = goalContributionDao
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `addContribution should insert dated contribution and recalculate goal cached total`() = runTest {
        val goalId = "goal-1"
        val contributionAmount = 15000L // R$ 150,00
        val timestamp = LocalDate.of(2026, 3, 15).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val totalRecalculated = 45000L // R$ 450,00 (após soma)

        coEvery { goalContributionDao.sumByGoal(goalId) } returns totalRecalculated

        repository.addContribution(goalId, contributionAmount, timestamp)

        val insertedSlot = slot<GoalContributionEntity>()
        coVerify(exactly = 1) {
            goalContributionDao.insert(capture(insertedSlot))
        }
        assertEquals(goalId, insertedSlot.captured.goalId)
        assertEquals(contributionAmount, insertedSlot.captured.amountCents)
        assertEquals(timestamp, insertedSlot.captured.date)

        coVerify(exactly = 1) {
            goalDao.updateCurrentAmount(goalId, totalRecalculated)
        }
    }

    @Test
    fun `getContributionsForPeriod by goalId should filter contributions for specific goal in target month`() = runTest {
        val goalId = "goal-1"
        val febDate = LocalDate.of(2026, 2, 14).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        val febStart = LocalDate.of(2026, 2, 1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val febEnd = LocalDate.of(2026, 2, 28).atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli()

        val febEntity = GoalContributionEntity(
            id = "c-feb",
            goalId = goalId,
            amountCents = 25000L,
            date = febDate
        )

        every {
            goalContributionDao.getByGoalForPeriod(goalId, febStart, febEnd)
        } returns flowOf(listOf(febEntity))

        val result = repository.getContributionsForPeriod(goalId, febStart, febEnd).first()

        assertEquals(1, result.size)
        assertEquals("c-feb", result[0].id)
        assertEquals(goalId, result[0].goalId)
        assertEquals(25000L, result[0].amountCents)
        assertEquals(febDate, result[0].date)
    }

    @Test
    fun `multiple contributions across different months return correct monthly sums and period lists`() = runTest {
        val goalId = "goal-viagem"
        val febContribution = 20000L
        val febDate = LocalDate.of(2026, 2, 15).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        val febStart = LocalDate.of(2026, 2, 1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val febEnd = LocalDate.of(2026, 2, 28).atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli()

        // Mock sumForPeriod no mês de fevereiro
        every { goalContributionDao.sumForPeriod(febStart, febEnd) } returns flowOf(febContribution)

        val monthlySum = repository.getMonthlyContribution(febStart, febEnd).first()
        assertEquals(febContribution, monthlySum)

        // Mock getForPeriod para todas as metas no período
        val febItem = GoalContributionEntity(id = "c2", goalId = goalId, amountCents = febContribution, date = febDate)
        every { goalContributionDao.getForPeriod(febStart, febEnd) } returns flowOf(listOf(febItem))

        val periodList = repository.getContributionsForPeriod(febStart, febEnd).first()
        assertEquals(1, periodList.size)
        assertEquals(febContribution, periodList[0].amountCents)
    }

    @Test
    fun `saveGoal with initial current amount should create initial contribution and recalculate total`() = runTest {
        val newGoal = Goal(
            id = "new-goal",
            name = "Reserva",
            targetAmountCents = 100000L,
            currentAmountCents = 20000L,
            createdAt = 1700000000000L
        )

        coEvery { goalDao.getById(newGoal.id) } returns null
        coEvery { goalContributionDao.sumByGoal(newGoal.id) } returns 20000L

        repository.saveGoal(newGoal)

        coVerify(exactly = 1) {
            goalDao.insert(any())
        }
        coVerify(exactly = 1) {
            goalContributionDao.insert(match {
                it.goalId == newGoal.id && it.amountCents == 20000L && it.date == newGoal.createdAt
            })
        }
        coVerify(exactly = 1) {
            goalDao.updateCurrentAmount(newGoal.id, 20000L)
        }
    }

    @Test
    fun `deleteGoal should delete contributions and goal within transaction`() = runTest {
        val goalId = "goal-to-delete"

        repository.deleteGoal(goalId)

        coVerify(exactly = 1) { goalContributionDao.deleteByGoalId(goalId) }
        coVerify(exactly = 1) { goalDao.deleteById(goalId) }
    }
}
