package com.platform.app.domain.model

data class DrawerOverview(
    val remainingMonthCents: Long = 0L,
    val overdueCount: Int = 0,
    val lastBackupTimestamp: Long = 0L,
    val initialScreen: String = "overview"
)
