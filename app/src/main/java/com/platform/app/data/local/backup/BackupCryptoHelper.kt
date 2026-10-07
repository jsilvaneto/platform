package com.platform.app.data.local.backup

import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utilitário criptográfico para exportação e importação segura de backups.
 *
 * Em conformidade com as diretrizes da skill security-guard:
 * - Algoritmo: AES-256-GCM (Autenticado, integridade e confidencialidade)
 * - Derivação de Chave: PBKDF2WithHmacSHA256 com sal aleatório e 65.536 iterações
 * - Limpeza defensiva de memória de senhas em char[]
 */
object BackupCryptoHelper {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KDF_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH_BITS = 256
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12

    private val secureRandom = SecureRandom()

    fun encrypt(plaintext: String, password: String): EncryptedBackupDto {
        require(password.isNotBlank()) { "A senha ou PIN de backup não pode estar vazia." }

        val salt = ByteArray(SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val secretKey = deriveKey(password.toCharArray(), salt, EncryptedBackupDto.DEFAULT_ITERATIONS)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return EncryptedBackupDto(
            saltBase64 = Base64.getEncoder().encodeToString(salt),
            ivBase64 = Base64.getEncoder().encodeToString(iv),
            ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertext),
            iterations = EncryptedBackupDto.DEFAULT_ITERATIONS
        )
    }

    fun decrypt(dto: EncryptedBackupDto, password: String): String {
        require(password.isNotBlank()) { "A senha ou PIN para restauração é obrigatória." }
        if (dto.format != EncryptedBackupDto.FORMAT_NAME) {
            throw IllegalArgumentException("Formato de backup incompatível ou desconhecido: ${dto.format}")
        }

        val salt = try {
            Base64.getDecoder().decode(dto.saltBase64)
        } catch (e: Exception) {
            throw IllegalArgumentException("Sal criptográfico corrompido.")
        }

        val iv = try {
            Base64.getDecoder().decode(dto.ivBase64)
        } catch (e: Exception) {
            throw IllegalArgumentException("Vetor de inicialização (IV) corrompido.")
        }

        val ciphertext = try {
            Base64.getDecoder().decode(dto.ciphertextBase64)
        } catch (e: Exception) {
            throw IllegalArgumentException("Criptograma do arquivo corrompido.")
        }

        val iterations = if (dto.iterations > 0) dto.iterations else EncryptedBackupDto.DEFAULT_ITERATIONS
        val secretKey = deriveKey(password.toCharArray(), salt, iterations)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        return try {
            val decryptedBytes = cipher.doFinal(ciphertext)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            throw SecurityException("Senha ou PIN incorreto. Não foi possível descriptografar o backup.", e)
        }
    }

    private fun deriveKey(passwordChars: CharArray, salt: ByteArray, iterations: Int): SecretKeySpec {
        val pbeSpec = PBEKeySpec(passwordChars, salt, iterations, KEY_LENGTH_BITS)
        try {
            val factory = SecretKeyFactory.getInstance(KDF_ALGORITHM)
            val secretKey = factory.generateSecret(pbeSpec)
            return SecretKeySpec(secretKey.encoded, "AES")
        } finally {
            pbeSpec.clearPassword()
            // Zera o array de caracteres para não permanecer em heap
            passwordChars.fill('0')
        }
    }
}
