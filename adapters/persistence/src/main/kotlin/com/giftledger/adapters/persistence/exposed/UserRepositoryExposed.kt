package com.giftledger.adapters.persistence.exposed

import com.giftledger.application.ports.UserRepository
import com.giftledger.domain.User
import com.giftledger.domain.UserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class UserRepositoryExposed : UserRepository {

    override fun create(email: String, passwordHash: String, username: String?, fullName: String?): User {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()
        val resolvedUsername = username ?: email.substringBefore("@")

        transaction {
            UsersTable.insert {
                it[UsersTable.id] = id.toString()
                it[UsersTable.username] = resolvedUsername
                it[UsersTable.email] = email
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.fullName] = fullName
                it[UsersTable.createdAt] = now
                it[UsersTable.updatedAt] = now
            }
        }

        return User(
            id = UserId(id),
            username = resolvedUsername,
            email = email,
            fullName = fullName,
            passwordHash = passwordHash,
            createdAt = now.toInstant(ZoneOffset.UTC),
            updatedAt = now.toInstant(ZoneOffset.UTC)
        )
    }

    override fun findById(id: UserId): User? = transaction {
        UsersTable.selectAll().where { UsersTable.id eq id.value.toString() }
            .map { it.toUser() }
            .singleOrNull()
    }

    override fun findByEmail(email: String): User? = transaction {
        UsersTable.selectAll().where { UsersTable.email eq email }
            .map { it.toUser() }
            .singleOrNull()
    }

    override fun updatePasswordHash(id: UserId, newPasswordHash: String): Boolean = transaction {
        val updated = UsersTable.update({ UsersTable.id eq id.value.toString() }) {
            it[passwordHash] = newPasswordHash
            it[updatedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    override fun delete(id: UserId): Boolean = transaction {
        val deleted = UsersTable.deleteWhere { UsersTable.id eq id.value.toString() }
        deleted > 0
    }


    override fun updateProfile(id: UserId, username: String?, email: String?, fullName: String?): User? = transaction {
        val existing = UsersTable.selectAll().where { UsersTable.id eq id.value.toString() }
            .map { it.toUser() }
            .singleOrNull() ?: return@transaction null

        UsersTable.update({ UsersTable.id eq id.value.toString() }) {
            username?.let { u -> it[UsersTable.username] = u }
            email?.let { e -> it[UsersTable.email] = e }
            fullName?.let { f -> it[UsersTable.fullName] = f }
            it[updatedAt] = LocalDateTime.now()
        }

        UsersTable.selectAll().where { UsersTable.id eq id.value.toString() }
            .map { it.toUser() }
            .singleOrNull()
    }

    private fun ResultRow.toUser(): User = User(
        id = UserId(UUID.fromString(this[UsersTable.id])),
        username = this[UsersTable.username],
        email = this[UsersTable.email],
        fullName = this[UsersTable.fullName],
        passwordHash = this[UsersTable.passwordHash],
        createdAt = this[UsersTable.createdAt].toInstant(ZoneOffset.UTC),
        updatedAt = this[UsersTable.updatedAt].toInstant(ZoneOffset.UTC)
    )
}
