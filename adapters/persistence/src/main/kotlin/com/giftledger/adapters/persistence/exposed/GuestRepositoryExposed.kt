package com.giftledger.adapters.persistence.exposed

import com.giftledger.application.ports.GuestRepository
import com.giftledger.domain.Guest
import com.giftledger.domain.GuestId
import com.giftledger.domain.UserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class GuestRepositoryExposed : GuestRepository {

    override fun create(userId: UserId, name: String, relationship: String, phone: String?, note: String?): Guest {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()

        transaction {
            GuestsTable.insert {
                it[GuestsTable.id] = id.toString()
                it[GuestsTable.userId] = userId.value.toString()
                it[GuestsTable.name] = name
                it[GuestsTable.relationship] = relationship
                it[GuestsTable.phone] = phone
                it[GuestsTable.note] = note
                it[GuestsTable.createdAt] = now
                it[GuestsTable.updatedAt] = now
            }
        }

        return Guest(
            id = GuestId(id),
            userId = userId,
            name = name,
            relationship = relationship,
            phone = phone,
            note = note,
            createdAt = now.toInstant(ZoneOffset.UTC),
            updatedAt = now.toInstant(ZoneOffset.UTC)
        )
    }

    override fun findById(userId: UserId, id: GuestId): Guest? = transaction {
        GuestsTable.selectAll()
            .where { (GuestsTable.id eq id.value.toString()) and (GuestsTable.userId eq userId.value.toString()) }
            .map { it.toGuest() }
            .singleOrNull()
    }

    override fun listByUserId(userId: UserId): List<Guest> = transaction {
        GuestsTable.selectAll()
            .where { GuestsTable.userId eq userId.value.toString() }
            .map { it.toGuest() }
    }


    override fun update(userId: UserId, id: GuestId, name: String?, relationship: String?, phone: String?, note: String?): Guest? = transaction {
        val existing = GuestsTable.selectAll()
            .where { (GuestsTable.id eq id.value.toString()) and (GuestsTable.userId eq userId.value.toString()) }
            .singleOrNull() ?: return@transaction null

        GuestsTable.update({ (GuestsTable.id eq id.value.toString()) and (GuestsTable.userId eq userId.value.toString()) }) {
            name?.let { n -> it[GuestsTable.name] = n }
            relationship?.let { r -> it[GuestsTable.relationship] = r }
            phone?.let { p -> it[GuestsTable.phone] = p }
            note?.let { n -> it[GuestsTable.note] = n }
            it[updatedAt] = LocalDateTime.now()
        }

        GuestsTable.selectAll()
            .where { GuestsTable.id eq id.value.toString() }
            .map { it.toGuest() }
            .singleOrNull()
    }

    override fun delete(userId: UserId, id: GuestId): Boolean = transaction {
        val deleted = GuestsTable.deleteWhere { 
            (GuestsTable.id eq id.value.toString()) and (GuestsTable.userId eq userId.value.toString()) 
        }
        deleted > 0
    }

    override fun listByUserIdPaginated(userId: UserId, page: Int, pageSize: Int, search: String?): Pair<List<Guest>, Int> = transaction {
        var query = GuestsTable.selectAll().where { GuestsTable.userId eq userId.value.toString() }
        
        search?.let { s ->
            query = query.andWhere { GuestsTable.name like "%$s%" }
        }

        val total = query.count().toInt()
        val offset = ((page - 1) * pageSize).toLong()
        
        val guests = query
            .limit(pageSize)
            .offset(offset)
            .map { it.toGuest() }

        Pair(guests, total)
    }

    private fun ResultRow.toGuest(): Guest = Guest(
        id = GuestId(UUID.fromString(this[GuestsTable.id])),
        userId = UserId(UUID.fromString(this[GuestsTable.userId])),
        name = this[GuestsTable.name],
        relationship = this[GuestsTable.relationship],
        phone = this[GuestsTable.phone],
        note = this[GuestsTable.note],
        createdAt = this[GuestsTable.createdAt].toInstant(ZoneOffset.UTC),
        updatedAt = this[GuestsTable.updatedAt].toInstant(ZoneOffset.UTC)
    )
}
