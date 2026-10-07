package com.platform.app.data.local.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertEquals(EncryptedBackupDto.ALGORITHM_NAME, encryptedDto.algorithm)
        assertEquals(EncryptedBackupDto.KDF_NAME, encryptedDto.kdf)
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
        assertTrue(exception.message!!.contains("Senha ou PIN incorreto"))
    }

    @Test
    fun `encrypt and decrypt with blank password throws IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupCryptoHelper.encrypt(samplePlaintext, "")
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
}
