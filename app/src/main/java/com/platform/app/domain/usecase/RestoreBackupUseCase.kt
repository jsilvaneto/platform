package com.platform.app.domain.usecase

import com.platform.app.domain.repository.BackupRepository
import javax.inject.Inject

class RestoreBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(encryptedBackupJson: String, password: String): Result<Unit> {
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("A senha para restauração é obrigatória."))
        }
        if (encryptedBackupJson.isBlank()) {
            return Result.failure(IllegalArgumentException("O arquivo de backup selecionado está vazio."))
        }
        return backupRepository.restoreBackupFromJson(encryptedBackupJson, password)
    }
}
