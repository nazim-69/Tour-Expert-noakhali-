package com.example.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 65536
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    fun generateSaltHex(): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return salt.toHexString()
    }

    fun hashPassword(password: String, saltHex: String): String {
        val salt = saltHex.hexToByteArray()
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return hash.toHexString()
    }

    fun verifyPassword(password: String, saltHex: String, expectedHashHex: String): Boolean {
        if (password.isBlank() || saltHex.isBlank() || expectedHashHex.isBlank()) return false
        return try {
            val actualHash = hashPassword(password, saltHex)
            MessageDigest.isEqual(
                actualHash.toByteArray(Charsets.UTF_8),
                expectedHashHex.toByteArray(Charsets.UTF_8)
            )
        } catch (_: Exception) {
            false
        }
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02x".format(it) }

    private fun String.hexToByteArray(): ByteArray {
        val clean = this.trim()
        require(clean.length % 2 == 0) { "Invalid hex string" }
        return ByteArray(clean.length / 2) { i ->
            clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
