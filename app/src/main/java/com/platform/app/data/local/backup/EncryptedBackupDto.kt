package com.platform.app.data.local.backup

import com.google.gson.annotations.SerializedName

/**
 * Envelope seguro para transporte e armazenamento de backups criptografados.
 *
 * Utiliza o padrão AEAD (Authenticated Encryption with Associated Data) via AES-256-GCM,
 * com chave simétrica derivada da senha/PIN do usuário via PBKDF2 com SHA-256.
 */
data class EncryptedBackupDto(
    @SerializedName("format") val format: String = FORMAT_NAME,
    @SerializedName("version") val version: Int = 1,
    @SerializedName("algorithm") val algorithm: String = ALGORITHM_NAME,
    @SerializedName("kdf") val kdf: String = KDF_NAME,
    @SerializedName("iterations") val iterations: Int = DEFAULT_ITERATIONS,
    @SerializedName("salt") val saltBase64: String,
    @SerializedName("iv") val ivBase64: String,
    @SerializedName("ciphertext") val ciphertextBase64: String,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val FORMAT_NAME = "PLATFORM_ENCRYPTED_BACKUP"
        const val ALGORITHM_NAME = "AES/GCM/NoPadding"
        const val KDF_NAME = "PBKDF2WithHmacSHA256"
        const val DEFAULT_ITERATIONS = 65536
    }
}
