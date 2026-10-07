package com.platform.app.domain.repository

interface BackupRepository {
    suspend fun exportBackupJson(password: String): Result<String>
    suspend fun restoreBackupFromJson(encryptedBackupJson: String, password: String): Result<Unit>
}
