package com.platform.app.domain.model

data class GoalContribution(
    val id: String,
    val goalId: String,
    val amountCents: Long,
    val date: Long,
    val createdAt: Long = date,
    val updatedAt: Long = date,
    val deletedAt: Long? = null
)
