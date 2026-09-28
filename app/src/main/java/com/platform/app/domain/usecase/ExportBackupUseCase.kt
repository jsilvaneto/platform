package com.platform.app.domain.usecase

import com.platform.app.domain.repository.BackupRepository
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(): Result<String> {
        return backupRepository.exportBackupJson()
    }
}
