package me.bossm0n5t3r.securities

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

@OptIn(ExperimentalStdlibApi::class)
object JwtProvider {
    const val ALGORITHM = "EC"
    private const val CURVE = "secp521r1"
    const val ISSUER = "realworld-kotlin-ktor-exposed-app"
    private const val EXPIRATION_TIME_IN_SECONDS = 3600L // 1 hour default

    var hexEncodedPublicKey: String
    var hexEncodedPrivateKey: String

    init {
        val (publicKey, privateKey) = generateECKeyPair()
        hexEncodedPublicKey = publicKey
        hexEncodedPrivateKey = privateKey
        println("Public key: $hexEncodedPublicKey")
        println("Private key: $hexEncodedPrivateKey")
        println("Public key (bytes): ${publicKey.hexToByteArray().contentToString()}")
        println("Private key (bytes): ${privateKey.hexToByteArray().contentToString()}")
        println("Public key (bytes length): ${publicKey.hexToByteArray().size}")
        println("Private key (bytes length): ${privateKey.hexToByteArray().size}")
    }

    fun generateECKeyPair(): Pair<String, String> {
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
        val ecSpec = ECGenParameterSpec(CURVE)
        keyPairGenerator.initialize(ecSpec)
        val keyPair = keyPairGenerator.generateKeyPair()
        val publicKey = keyPair.public.encoded.toHexString()
        val privateKey = keyPair.private.encoded.toHexString()
        return publicKey to privateKey
    }

    fun String.toPublicKey(): PublicKey {
        val keySpec = X509EncodedKeySpec(hexToByteArray())
        return KeyFactory.getInstance(ALGORITHM).generatePublic(keySpec)
    }

    fun String.toPrivateKey(): PrivateKey {
        val keySpec = PKCS8EncodedKeySpec(hexToByteArray())
        return KeyFactory.getInstance(ALGORITHM).generatePrivate(keySpec)
    }

    fun createJWT(
        publicKey: PublicKey,
        privateKey: PrivateKey,
        issuer: String = ISSUER,
        subject: String,
        expirationTimeInSeconds: Long = EXPIRATION_TIME_IN_SECONDS,
    ): String =
        try {
            val algorithm = Algorithm.ECDSA512(publicKey as ECPublicKey, privateKey as ECPrivateKey)
            val currentTimeMillis = System.currentTimeMillis()
            JWT
                .create()
                .withIssuer(issuer)
                .withSubject(subject)
                .withIssuedAt(java.util.Date(currentTimeMillis))
                .withExpiresAt(java.util.Date(currentTimeMillis + expirationTimeInSeconds * 1000))
                .sign(algorithm)
        } catch (e: JWTCreationException) {
            throw RuntimeException("Token creation failed: ${e.message}", e)
        } catch (e: ClassCastException) {
            throw RuntimeException("Invalid key type. Expected EC keys.", e)
        }

    fun verifyJWT(
        token: String,
        publicKey: PublicKey,
        issuer: String = ISSUER,
    ): DecodedJWT =
        try {
            val algorithm = Algorithm.ECDSA512(publicKey as ECPublicKey, null)
            val verifier: JWTVerifier =
                JWT
                    .require(algorithm)
                    .withIssuer(issuer)
                    .build()
            verifier.verify(token)
        } catch (e: JWTVerificationException) {
            throw RuntimeException("Token verification failed: ${e.message}", e)
        } catch (e: ClassCastException) {
            throw RuntimeException("Invalid key type. Expected EC public key.", e)
        } catch (e: IllegalArgumentException) {
            throw RuntimeException("Invalid token format: ${e.message}", e)
        }
}
