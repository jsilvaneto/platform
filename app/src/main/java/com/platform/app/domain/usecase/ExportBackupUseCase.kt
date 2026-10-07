package com.platform.app.domain.usecase

import com.platform.app.domain.repository.BackupRepository
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(password: String): Result<String> {
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("A senha ou PIN de backup não pode estar vazia."))
        }
        return backupRepository.exportBackupJson(password)
    }
}
