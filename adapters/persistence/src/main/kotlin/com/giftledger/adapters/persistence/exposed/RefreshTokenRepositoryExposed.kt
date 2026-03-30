package com.giftledger.adapters.persistence.exposed

import com.giftledger.application.ports.RefreshTokenRepository
import com.giftledger.domain.RefreshToken
import com.giftledger.domain.UserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class RefreshTokenRepositoryExposed : RefreshTokenRepository {

    override fun create(userId: UserId, tokenHash: String, expiresAt: Instant, userAgent: String?, ip: String?): RefreshToken {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()

        transaction {
            RefreshTokensTable.insert {
                it[RefreshTokensTable.id] = id.toString()
                it[RefreshTokensTable.userId] = userId.value.toString()
                it[RefreshTokensTable.tokenHash] = tokenHash
                it[RefreshTokensTable.expiresAt] = LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC)
                it[RefreshTokensTable.revokedAt] = null
                it[RefreshTokensTable.userAgent] = userAgent
                it[RefreshTokensTable.ip] = ip
                it[RefreshTokensTable.createdAt] = now
            }
        }

        return RefreshToken(
            id = id,
            userId = userId,
            tokenHash = tokenHash,
            expiresAt = expiresAt,
            revokedAt = null,
            userAgent = userAgent,
            ip = ip,
            createdAt = now.toInstant(ZoneOffset.UTC)
        )
    }

    override fun findByTokenHash(tokenHash: String): RefreshToken? = transaction {
        RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.tokenHash eq tokenHash }
            .map { it.toRefreshToken() }
            .singleOrNull()
    }

    override fun revoke(id: UUID): Boolean = transaction {
        val updated = RefreshTokensTable.update({ RefreshTokensTable.id eq id.toString() }) {
            it[revokedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    private fun ResultRow.toRefreshToken(): RefreshToken = RefreshToken(
        id = UUID.fromString(this[RefreshTokensTable.id]),
        userId = UserId(UUID.fromString(this[RefreshTokensTable.userId])),
        tokenHash = this[RefreshTokensTable.tokenHash],
        expiresAt = this[RefreshTokensTable.expiresAt].toInstant(ZoneOffset.UTC),
        revokedAt = this[RefreshTokensTable.revokedAt]?.toInstant(ZoneOffset.UTC),
        userAgent = this[RefreshTokensTable.userAgent],
        ip = this[RefreshTokensTable.ip],
        createdAt = this[RefreshTokensTable.createdAt].toInstant(ZoneOffset.UTC)
    )
}
