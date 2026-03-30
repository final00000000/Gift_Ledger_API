package com.giftledger.application.ports

import com.giftledger.domain.RefreshToken
import com.giftledger.domain.UserId
import java.time.Instant
import java.util.UUID

interface RefreshTokenRepository {
    fun create(userId: UserId, tokenHash: String, expiresAt: Instant, userAgent: String?, ip: String?): RefreshToken
    fun findByTokenHash(tokenHash: String): RefreshToken?
    fun revoke(id: UUID): Boolean
}

