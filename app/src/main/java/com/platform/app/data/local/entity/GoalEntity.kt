package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Goal

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val targetAmountCents: Long,
    val currentAmountCents: Long,
    val deadlineDate: Long?,
    val colorHex: String,
    val createdAt: Long
) {
    fun toDomain(): Goal = Goal(
        id = id,
        name = name,
        targetAmountCents = targetAmountCents,
        currentAmountCents = currentAmountCents,
        deadlineDate = deadlineDate,
        colorHex = colorHex,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(goal: Goal): GoalEntity = GoalEntity(
            id = goal.id,
            name = goal.name,
            targetAmountCents = goal.targetAmountCents,
            currentAmountCents = goal.currentAmountCents,
            deadlineDate = goal.deadlineDate,
            colorHex = goal.colorHex,
            createdAt = goal.createdAt
        )
    }
}
