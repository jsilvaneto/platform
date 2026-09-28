package com.platform.app.domain.usecase

import com.platform.app.domain.repository.BackupRepository
import javax.inject.Inject

class RestoreBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(jsonString: String): Result<Unit> {
        return backupRepository.restoreBackupFromJson(jsonString)
    }
}
