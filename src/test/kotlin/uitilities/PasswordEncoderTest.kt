package me.bossm0n5t3r.uitilities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordEncoderTest {
    private val passwordEncoder: PasswordEncoder = PasswordEncoderImpl()

    @Test
    fun testGenerateHexEncodedSalt() {
        // When generating a salt
        val salt = passwordEncoder.generateHexEncodedSalt()

        // Then the salt should not be empty
        assertTrue(salt.isNotEmpty())

        // And the salt should be a valid hex string
        assertTrue(salt.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' })

        // And generating another salt should produce a different result
        val anotherSalt = passwordEncoder.generateHexEncodedSalt()
        assertNotEquals(salt, anotherSalt)
    }

    @Test
    fun testEncode() {
        // Given a raw password
        val rawPassword = "password123"

        // When encoding the password
        val (encodedPassword, salt) = passwordEncoder.encode(rawPassword)

        // Then the encoded password should not be empty
        assertTrue(encodedPassword.isNotEmpty())

        // And the salt should not be empty
        assertTrue(salt.isNotEmpty())

        // And encoding the same password again should produce a different result
        val (anotherEncodedPassword, anotherSalt) = passwordEncoder.encode(rawPassword)
        assertNotEquals(encodedPassword, anotherEncodedPassword)
        assertNotEquals(salt, anotherSalt)
    }

    @Test
    fun testMatches() {
        // Given a raw password
        val rawPassword = "password123"

        // And an encoded password with salt
        val (encodedPassword, salt) = passwordEncoder.encode(rawPassword)

        // When checking if the raw password matches the encoded password
        val matches = passwordEncoder.matches(rawPassword, encodedPassword, salt)

        // Then the result should be true
        assertTrue(matches)

        // And when checking with a different password
        val differentPassword = "differentPassword"
        val matchesWithDifferentPassword = passwordEncoder.matches(differentPassword, encodedPassword, salt)

        // Then the result should be false
        assertFalse(matchesWithDifferentPassword)
    }

    @Test
    fun testHashPassword() {
        // Given a password and salt
        val password = "password123"
        val salt = passwordEncoder.generateHexEncodedSalt()

        // When hashing the password
        val hashedPassword = passwordEncoder.hashPassword(password, salt)

        // Then the hashed password should not be empty
        assertTrue(hashedPassword.isNotEmpty())

        // And hashing the same password with the same salt should produce the same result (deterministic)
        val sameHashedPassword = passwordEncoder.hashPassword(password, salt)
        assertEquals(hashedPassword, sameHashedPassword)

        // And hashing a different password with the same salt should produce a different result
        val differentPassword = "differentPassword"
        val differentHashedPassword = passwordEncoder.hashPassword(differentPassword, salt)
        assertNotEquals(hashedPassword, differentHashedPassword)

        // And hashing the same password with a different salt should produce a different result
        val differentSalt = passwordEncoder.generateHexEncodedSalt()
        val hashedPasswordWithDifferentSalt = passwordEncoder.hashPassword(password, differentSalt)
        assertNotEquals(hashedPassword, hashedPasswordWithDifferentSalt)
    }
}
