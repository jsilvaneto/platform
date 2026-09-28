package com.platform.app.domain.repository

interface BackupRepository {
    suspend fun exportBackupJson(): Result<String>
    suspend fun restoreBackupFromJson(jsonString: String): Result<Unit>
}
