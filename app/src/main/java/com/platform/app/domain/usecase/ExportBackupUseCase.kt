package com.platform.app.domain.usecase

import com.platform.app.domain.repository.BackupRepository
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(password: String): Result<String> {
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("A senha de backup não pode estar vazia."))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("A senha de backup deve possuir no mínimo 8 caracteres."))
        }
        return backupRepository.exportBackupJson(password)
    }
}
