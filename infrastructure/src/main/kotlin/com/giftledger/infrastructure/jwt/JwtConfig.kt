package com.giftledger.infrastructure.jwt

import com.giftledger.infrastructure.config.Env

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessTokenTtlSeconds: Long,
    val refreshTokenTtlSeconds: Long,
) {
    init {
        require(secret.isNotBlank()) { "secret must not be blank" }
        require(accessTokenTtlSeconds > 0) { "accessTokenTtlSeconds must be > 0" }
        require(refreshTokenTtlSeconds > 0) { "refreshTokenTtlSeconds must be > 0" }
    }

    companion object {
        fun fromEnv(env: Map<String, String> = System.getenv()): JwtConfig =
            JwtConfig(
                secret = Env.string(env, "JWT_SECRET", "dev-secret-change-me"),
                issuer = Env.string(env, "JWT_ISSUER", "gift-ledger"),
                audience = Env.string(env, "JWT_AUDIENCE", "gift-ledger"),
                realm = Env.string(env, "JWT_REALM", "gift-ledger"),
                accessTokenTtlSeconds = Env.long(env, "JWT_ACCESS_TTL_SECONDS", 15 * 60L),
                refreshTokenTtlSeconds = Env.long(env, "JWT_REFRESH_TTL_SECONDS", 30L * 24 * 60 * 60),
            )
    }
}
