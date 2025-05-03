package me.bossm0n5t3r

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import me.bossm0n5t3r.configurations.LOGGER
import me.bossm0n5t3r.securities.JwtProvider
import me.bossm0n5t3r.securities.JwtProvider.ISSUER
import me.bossm0n5t3r.securities.JwtProvider.toPublicKey
import java.security.interfaces.ECPublicKey

fun Application.configureSecurity() {
    val algorithm = Algorithm.ECDSA512(JwtProvider.hexEncodedPublicKey.toPublicKey() as ECPublicKey, null)
    authentication {
        jwt {
            verifier(
                JWT
                    .require(algorithm)
                    .withIssuer(ISSUER)
                    .build(),
            )
            authSchemes("Token")
            validate { credential ->
                val subject = credential.subject
                LOGGER.info("Subject: $subject")
                if (credential.payload.issuer.equals(ISSUER) && subject != null) UserIdPrincipal(subject) else null
            }
        }
    }
}

fun ApplicationCall.userId() = principal<UserIdPrincipal>()?.name ?: error("No user ID found")
