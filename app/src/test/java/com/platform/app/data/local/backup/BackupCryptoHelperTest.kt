package com.platform.app.data.local.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class BackupCryptoHelperTest {

    private val samplePlaintext = """
        {
          "version": 2,
          "categories": [{"id": "cat-1", "name": "Alimentação"}],
          "financialAccounts": [{"id": "acc-1", "name": "Nubank PF", "accountType": "CORRENTE"}],
          "bills": [{"id": "bill-1", "title": "Copel Energia", "totalAmountCents": 25000}]
        }
    """.trimIndent()

    private val validPassword = "MinhaSenhaForte@2026"
    private val wrongPassword = "SenhaIncorreta@123"

    @Test
    fun `encrypt and decrypt cycle with correct password recovers exact plaintext`() {
        val encryptedDto = BackupCryptoHelper.encrypt(samplePlaintext, validPassword)

        assertEquals(EncryptedBackupDto.FORMAT_NAME, encryptedDto.format)
        assertEquals(2, encryptedDto.version)
        assertEquals(EncryptedBackupDto.ALGORITHM_NAME, encryptedDto.algorithm)
        assertEquals(EncryptedBackupDto.KDF_NAME, encryptedDto.kdf)
        assertEquals(600_000, encryptedDto.iterations)
        assertTrue(encryptedDto.saltBase64.isNotBlank())
        assertTrue(encryptedDto.ivBase64.isNotBlank())
        assertTrue(encryptedDto.ciphertextBase64.isNotBlank())

        val decrypted = BackupCryptoHelper.decrypt(encryptedDto, validPassword)
        assertEquals(samplePlaintext, decrypted)
    }

    @Test
    fun `ciphertext does not leak any sensitive plaintext tokens`() {
        val encryptedDto = BackupCryptoHelper.encrypt(samplePlaintext, validPassword)

        assertFalse("Ciphertext não pode conter tokens em texto claro", encryptedDto.ciphertextBase64.contains("Nubank"))
        assertFalse("Ciphertext não pode conter tokens em texto claro", encryptedDto.ciphertextBase64.contains("Copel"))
        assertFalse("Ciphertext não pode conter tokens em texto claro", encryptedDto.ciphertextBase64.contains("Alimentação"))
    }

    @Test
    fun `decrypt with wrong password throws SecurityException`() {
        val encryptedDto = BackupCryptoHelper.encrypt(samplePlaintext, validPassword)

        val exception = assertThrows(SecurityException::class.java) {
            BackupCryptoHelper.decrypt(encryptedDto, wrongPassword)
        }
        assertTrue(exception.message!!.contains("Senha incorreta"))
    }

    @Test
    fun `encrypt and decrypt with blank or short password throws IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupCryptoHelper.encrypt(samplePlaintext, "")
        }

        // Senha com menos de 8 caracteres
        assertThrows(IllegalArgumentException::class.java) {
            BackupCryptoHelper.encrypt(samplePlaintext, "1234567")
        }

        val encryptedDto = BackupCryptoHelper.encrypt(samplePlaintext, validPassword)
        assertThrows(IllegalArgumentException::class.java) {
            BackupCryptoHelper.decrypt(encryptedDto, "   ")
        }
    }

    @Test
    fun `decrypt with corrupted ciphertext throws SecurityException or IllegalArgumentException`() {
        val encryptedDto = BackupCryptoHelper.encrypt(samplePlaintext, validPassword)
        val tamperedDto = encryptedDto.copy(
            ciphertextBase64 = encryptedDto.ciphertextBase64.substring(0, encryptedDto.ciphertextBase64.length - 4) + "AAAA"
        )

        assertThrows(Exception::class.java) {
            BackupCryptoHelper.decrypt(tamperedDto, validPassword)
        }
    }

    @Test
    fun `decrypt with tampered header in v2 backup fails due to AAD authentication`() {
        val encryptedDto = BackupCryptoHelper.encrypt(samplePlaintext, validPassword)

        // 1. Modificar version de 2 para 1 faz falhar a verificação da tag GCM por ausência de AAD
        val tamperedVersionTo1 = encryptedDto.copy(version = 1)
        assertThrows(SecurityException::class.java) {
            BackupCryptoHelper.decrypt(tamperedVersionTo1, validPassword)
        }

        // 2. Modificar version para versão não suportada
        val tamperedVersionTo99 = encryptedDto.copy(version = 99)
        assertThrows(IllegalArgumentException::class.java) {
            BackupCryptoHelper.decrypt(tamperedVersionTo99, validPassword)
        }

        // 3. Modificar iterations dentro da faixa permitida (ex: 500_000) altera o AAD e falha a tag GCM
        val tamperedIterations = encryptedDto.copy(iterations = 500_000)
        assertThrows(SecurityException::class.java) {
            BackupCryptoHelper.decrypt(tamperedIterations, validPassword)
        }

        // 4. Modificar format rejeita imediatamente
        val tamperedFormat = encryptedDto.copy(format = "OUTRO_FORMATO")
        assertThrows(IllegalArgumentException::class.java) {
            BackupCryptoHelper.decrypt(tamperedFormat, validPassword)
        }
    }

    @Test
    fun `decrypt with iterations outside acceptable range throws IllegalArgumentException`() {
        val encryptedDto = BackupCryptoHelper.encrypt(samplePlaintext, validPassword)

        // Abaixo do mínimo permitido (10_000)
        val lowIterations = encryptedDto.copy(iterations = 9_999)
        val exLow = assertThrows(IllegalArgumentException::class.java) {
            BackupCryptoHelper.decrypt(lowIterations, validPassword)
        }
        assertTrue(exLow.message!!.contains("limites permitidos"))

        // Acima do máximo permitido (2_000_000) - proteção contra DoS / travamento
        val highIterations = encryptedDto.copy(iterations = 2_000_001)
        val exHigh = assertThrows(IllegalArgumentException::class.java) {
            BackupCryptoHelper.decrypt(highIterations, validPassword)
        }
        assertTrue(exHigh.message!!.contains("limites permitidos"))
    }

    @Test
    fun `decrypt legacy v1 backup without AAD restores exact plaintext successfully`() {
        val legacyV1Dto = createLegacyV1Backup(samplePlaintext, validPassword, iterations = 65536)

        assertEquals(1, legacyV1Dto.version)
        assertEquals(65536, legacyV1Dto.iterations)

        val decrypted = BackupCryptoHelper.decrypt(legacyV1Dto, validPassword)
        assertEquals(samplePlaintext, decrypted)
    }

    @Test
    fun `decrypt legacy v1 backup with wrong password throws SecurityException`() {
        val legacyV1Dto = createLegacyV1Backup(samplePlaintext, validPassword, iterations = 65536)

        val ex = assertThrows(SecurityException::class.java) {
            BackupCryptoHelper.decrypt(legacyV1Dto, wrongPassword)
        }
        assertTrue(ex.message!!.contains("Senha incorreta"))
    }

    @Test
    fun `decrypt legacy v1 backup with corrupted ciphertext throws Exception`() {
        val legacyV1Dto = createLegacyV1Backup(samplePlaintext, validPassword, iterations = 65536)
        val tampered = legacyV1Dto.copy(
            ciphertextBase64 = legacyV1Dto.ciphertextBase64.substring(0, legacyV1Dto.ciphertextBase64.length - 4) + "ZZZZ"
        )

        assertThrows(Exception::class.java) {
            BackupCryptoHelper.decrypt(tampered, validPassword)
        }
    }

    private fun createLegacyV1Backup(plaintext: String, password: String, iterations: Int = 65536): EncryptedBackupDto {
        val random = SecureRandom()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) }
        val pbeSpec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(pbeSpec).encoded, "AES")
        pbeSpec.clearPassword()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        // Sem chamada de cipher.updateAAD - legacy v1 não usava AAD
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return EncryptedBackupDto(
            format = EncryptedBackupDto.FORMAT_NAME,
            version = 1,
            algorithm = EncryptedBackupDto.ALGORITHM_NAME,
            kdf = EncryptedBackupDto.KDF_NAME,
            iterations = iterations,
            saltBase64 = Base64.getEncoder().encodeToString(salt),
            ivBase64 = Base64.getEncoder().encodeToString(iv),
            ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertext),
            createdAt = System.currentTimeMillis()
        )
    }
}
