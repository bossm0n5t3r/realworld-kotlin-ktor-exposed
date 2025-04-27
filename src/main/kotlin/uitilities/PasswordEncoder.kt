package me.bossm0n5t3r.uitilities

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

interface PasswordEncoder {
    fun generateHexEncodedSalt(): String

    fun hashPassword(
        password: String,
        salt: String,
    ): String

    fun encode(rawPassword: String): Pair<String, String>

    fun matches(
        rawPassword: String,
        encodedPassword: String,
        hexEncodedSalt: String,
    ): Boolean
}

@OptIn(ExperimentalStdlibApi::class)
class PasswordEncoderImpl : PasswordEncoder {
    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA512"
        private const val ITERATIONS = 120_000 // OWASP 2023 권장 값
        private const val KEY_LENGTH = 256
        private val SECURE_RANDOM = SecureRandom.getInstanceStrong()
    }

    override fun generateHexEncodedSalt(): String {
        val salt = ByteArray(16)
        SECURE_RANDOM.nextBytes(salt)
        return salt.toHexString()
    }

    override fun hashPassword(
        password: String,
        hexEncodedSalt: String,
    ): String {
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val spec =
            PBEKeySpec(
                password.toCharArray(),
                hexEncodedSalt.hexToByteArray(),
                ITERATIONS,
                KEY_LENGTH,
            )

        val key = factory.generateSecret(spec)
        return key.encoded.toHexString()
    }

    private fun constantTimeEquals(
        a: String,
        b: String,
    ): Boolean = MessageDigest.isEqual(a.toByteArray(), b.toByteArray())

    override fun encode(rawPassword: String): Pair<String, String> {
        val salt = generateHexEncodedSalt()
        val encodedPassword = hashPassword(rawPassword, salt)
        return encodedPassword to salt
    }

    override fun matches(
        rawPassword: String,
        encodedPassword: String,
        hexEncodedSalt: String,
    ): Boolean = constantTimeEquals(hashPassword(rawPassword, hexEncodedSalt), encodedPassword)
}
