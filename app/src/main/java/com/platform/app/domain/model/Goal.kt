package com.platform.app.domain.model

data class Goal(
    val id: String,
    val name: String,
    val targetAmountCents: Long,
    val currentAmountCents: Long = 0L,
    val deadlineDate: Long? = null,
    val colorHex: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercentage: Float
        get() = if (targetAmountCents > 0L) {
            (currentAmountCents.toFloat() / targetAmountCents.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val remainingAmountCents: Long
        get() = (targetAmountCents - currentAmountCents).coerceAtLeast(0L)

    val isCompleted: Boolean
        get() = currentAmountCents >= targetAmountCents && targetAmountCents > 0L
}
