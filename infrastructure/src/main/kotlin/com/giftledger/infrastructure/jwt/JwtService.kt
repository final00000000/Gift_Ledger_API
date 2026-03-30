package com.giftledger.infrastructure.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Clock
import java.util.Date
import java.util.UUID

interface JwtTokenProvider {
    fun generateAccessToken(userId: String): String
    fun generateRefreshToken(userId: String): String
}

class JwtService(
    private val config: JwtConfig = JwtConfig.fromEnv(),
    private val clock: Clock = Clock.systemUTC(),
) : JwtTokenProvider {
    private val algorithm: Algorithm = Algorithm.HMAC256(config.secret)

    private val verifier = JWT.require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .build()

    override fun generateAccessToken(userId: String): String =
        generateToken(userId = userId, ttlSeconds = config.accessTokenTtlSeconds, type = "access")

    override fun generateRefreshToken(userId: String): String =
        generateToken(userId = userId, ttlSeconds = config.refreshTokenTtlSeconds, type = "refresh")

    fun verifyToken(token: String): DecodedJWT? =
        try {
            verifier.verify(token)
        } catch (_: JWTVerificationException) {
            null
        }

    private fun generateToken(userId: String, ttlSeconds: Long, type: String): String {
        val now = clock.instant()
        val expiresAt = now.plusSeconds(ttlSeconds)

        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .withJWTId(UUID.randomUUID().toString())
            .withClaim("type", type)
            .withClaim("uid", userId)
            .sign(algorithm)
    }
}
