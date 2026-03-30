package com.giftledger.domain

import java.time.Instant
import java.util.UUID

@JvmInline value class RefreshTokenId(val value: UUID)

data class RefreshToken(
    val id: UUID,
    val userId: UserId,
    val tokenHash: String,
    val expiresAt: Instant,
    val revokedAt: Instant?,
    val userAgent: String?,
    val ip: String?,
    val createdAt: Instant,
)
