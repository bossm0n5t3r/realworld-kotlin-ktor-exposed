package me.bossm0n5t3r.securities

import me.bossm0n5t3r.securities.JwtProvider.ALGORITHM
import me.bossm0n5t3r.securities.JwtProvider.ISSUER
import me.bossm0n5t3r.securities.JwtProvider.toPrivateKey
import me.bossm0n5t3r.securities.JwtProvider.toPublicKey
import java.security.PrivateKey
import java.security.PublicKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class JwtProviderTest {
    @Test
    fun testGenerateECKeyPair() {
        // When generating an EC key pair
        val (publicKey, privateKey) = JwtProvider.generateECKeyPair()

        // Then both keys should not be empty
        assertTrue(publicKey.isNotEmpty())
        assertTrue(privateKey.isNotEmpty())

        // And generating another key pair should produce different results
        val (anotherPublicKey, anotherPrivateKey) = JwtProvider.generateECKeyPair()
        assertNotEquals(publicKey, anotherPublicKey)
        assertNotEquals(privateKey, anotherPrivateKey)
    }

    @Test
    fun testToPublicKey() {
        // Given a public key string from a generated key pair
        val (publicKeyString, _) = JwtProvider.generateECKeyPair()

        // When converting it to a PublicKey object
        val publicKey = publicKeyString.toPublicKey()

        // Then the result should be a valid PublicKey
        assertNotNull(publicKey)
        assertEquals(ALGORITHM, publicKey.algorithm)
    }

    @Test
    fun testToPrivateKey() {
        // Given a private key string from a generated key pair
        val (_, privateKeyString) = JwtProvider.generateECKeyPair()

        // When converting it to a PrivateKey object
        val privateKey = privateKeyString.toPrivateKey()

        // Then the result should be a valid PrivateKey
        assertNotNull(privateKey)
        assertEquals(ALGORITHM, privateKey.algorithm)
    }

    @Test
    fun testCreateJWT() {
        // Given a key pair
        val (publicKeyString, privateKeyString) = JwtProvider.generateECKeyPair()
        val publicKey = publicKeyString.toPublicKey()
        val privateKey = privateKeyString.toPrivateKey()

        // When creating a JWT
        val jwt = JwtProvider.createJWT(publicKey, privateKey, subject = "test-subject")

        // Then the JWT should not be empty
        assertTrue(jwt.isNotEmpty())
    }

    @Test
    fun testCreateJWTWithCustomParameters() {
        // Given a key pair
        val (publicKeyString, privateKeyString) = JwtProvider.generateECKeyPair()
        val publicKey = publicKeyString.toPublicKey()
        val privateKey = privateKeyString.toPrivateKey()

        // And custom parameters
        val customIssuer = "custom-issuer"
        val customSubject = "custom-subject"
        val customExpirationTime = 7200L // 2 hours

        // When creating a JWT with custom parameters
        val jwt =
            JwtProvider.createJWT(
                publicKey,
                privateKey,
                customIssuer,
                customSubject,
                customExpirationTime,
            )

        // Then the JWT should not be empty
        assertTrue(jwt.isNotEmpty())

        // And when verifying the JWT with the custom issuer
        val decodedJWT = JwtProvider.verifyJWT(jwt, publicKey, customIssuer)

        // Then the decoded JWT should have the custom values
        assertEquals(customIssuer, decodedJWT.issuer)
        assertEquals(customSubject, decodedJWT.subject)

        // And the expiration time should be approximately 2 hours from now
        val now = System.currentTimeMillis()
        val expirationTime = decodedJWT.expiresAt.time
        val timeDifference = expirationTime - now

        // Allow for a small margin of error (5 seconds)
        assertTrue(timeDifference > 0)
        assertTrue(timeDifference <= customExpirationTime * 1000 + 5000)
        assertTrue(timeDifference >= customExpirationTime * 1000 - 5000)
    }

    @Test
    fun testVerifyJWT() {
        // Given a key pair
        val (publicKeyString, privateKeyString) = JwtProvider.generateECKeyPair()
        val publicKey = publicKeyString.toPublicKey()
        val privateKey = privateKeyString.toPrivateKey()
        val subject = "test-subject"

        // And a JWT created with those keys
        val jwt = JwtProvider.createJWT(publicKey, privateKey, subject = subject)

        // When verifying the JWT
        val decodedJWT = JwtProvider.verifyJWT(jwt, publicKey)

        // Then the decoded JWT should be valid
        assertNotNull(decodedJWT)
        assertEquals(ISSUER, decodedJWT.issuer)
        assertEquals(subject, decodedJWT.subject)
    }

    @Test
    fun testJWTVerificationWithWrongKey() {
        // Given a key pair
        val (publicKeyString, privateKeyString) = JwtProvider.generateECKeyPair()
        val publicKey = publicKeyString.toPublicKey()
        val privateKey = privateKeyString.toPrivateKey()

        // And a JWT created with those keys
        val jwt = JwtProvider.createJWT(publicKey, privateKey, subject = "test-subject")

        // And another key pair
        val (anotherPublicKeyString, _) = JwtProvider.generateECKeyPair()
        val anotherPublicKey = anotherPublicKeyString.toPublicKey()

        // When verifying the JWT with the wrong public key
        try {
            JwtProvider.verifyJWT(jwt, anotherPublicKey)
            fail("Expected an exception to be thrown")
        } catch (e: RuntimeException) {
            // Then an exception should be thrown
            assertTrue(e.message?.contains("Token verification failed") ?: false)
        }
    }

    @Test
    fun testErrorHandlingInCreateJWT() {
        // Given an invalid key pair (not EC keys)
        val invalidPublicKey =
            object : PublicKey {
                override fun getAlgorithm(): String = "RSA"

                override fun getFormat(): String = "X.509"

                override fun getEncoded(): ByteArray = ByteArray(0)
            }
        val invalidPrivateKey =
            object : PrivateKey {
                override fun getAlgorithm(): String = "RSA"

                override fun getFormat(): String = "PKCS#8"

                override fun getEncoded(): ByteArray = ByteArray(0)
            }

        // When creating a JWT with invalid keys
        try {
            JwtProvider.createJWT(invalidPublicKey, invalidPrivateKey, subject = "test-subject")
            fail("Expected an exception to be thrown")
        } catch (e: RuntimeException) {
            // Then an exception should be thrown
            assertTrue(e.message?.contains("Invalid key type") ?: false)
        }
    }

    @Test
    fun testErrorHandlingInVerifyJWT() {
        // Given an invalid token
        val invalidToken = "invalid.token.format"

        // And a valid key pair
        val (publicKeyString, _) = JwtProvider.generateECKeyPair()
        val publicKey = publicKeyString.toPublicKey()

        // When verifying the invalid token
        try {
            JwtProvider.verifyJWT(invalidToken, publicKey)
            fail("Expected an exception to be thrown")
        } catch (e: RuntimeException) {
            // Then an exception should be thrown
            assertTrue(e.message?.contains("Invalid token format") ?: false || e.message?.contains("Token verification failed") ?: false)
        }

        // Given an invalid key (not an EC key)
        val invalidPublicKey =
            object : PublicKey {
                override fun getAlgorithm(): String = "RSA"

                override fun getFormat(): String = "X.509"

                override fun getEncoded(): ByteArray = ByteArray(0)
            }

        // And a valid token
        val (_, privateKeyString) = JwtProvider.generateECKeyPair()
        val privateKey = privateKeyString.toPrivateKey()
        val validToken = JwtProvider.createJWT(publicKey, privateKey, subject = "test-subject")

        // When verifying a valid token with an invalid key
        try {
            JwtProvider.verifyJWT(validToken, invalidPublicKey)
            fail("Expected an exception to be thrown")
        } catch (e: RuntimeException) {
            // Then an exception should be thrown
            assertTrue(e.message?.contains("Invalid key type") ?: false)
        }
    }

    @Test
    fun testFullJWTWorkflow() {
        // Given a key pair
        val (publicKeyString, privateKeyString) = JwtProvider.generateECKeyPair()
        val publicKey = publicKeyString.toPublicKey()
        val privateKey = privateKeyString.toPrivateKey()
        val subject = "test-subject"

        // When creating a JWT
        val jwt = JwtProvider.createJWT(publicKey, privateKey, subject = subject)

        // And verifying it with the same public key
        val decodedJWT = JwtProvider.verifyJWT(jwt, publicKey)

        // Then the decoded JWT should be valid
        assertNotNull(decodedJWT)
        assertEquals(ISSUER, decodedJWT.issuer)
        assertEquals(subject, decodedJWT.subject)
    }
}
