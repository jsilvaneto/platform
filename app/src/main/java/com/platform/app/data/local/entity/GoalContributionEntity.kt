package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.platform.app.domain.model.GoalContribution
import java.util.UUID

@Entity(
    tableName = "goal_contributions",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("goalId"),
        Index("date")
    ]
)
data class GoalContributionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val amountCents: Long,
    val date: Long = System.currentTimeMillis()
) {
    fun toDomain(): GoalContribution = GoalContribution(
        id = id,
        goalId = goalId,
        amountCents = amountCents,
        date = date
    )

    companion object {
        fun fromDomain(domain: GoalContribution): GoalContributionEntity = GoalContributionEntity(
            id = domain.id,
            goalId = domain.goalId,
            amountCents = domain.amountCents,
            date = domain.date
        )
    }
}
