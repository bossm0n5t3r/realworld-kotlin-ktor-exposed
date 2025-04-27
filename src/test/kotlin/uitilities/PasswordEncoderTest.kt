package me.bossm0n5t3r.uitilities

import kotlin.test.Test
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
}
