package com.giftledger.adapters.persistence.exposed

import com.giftledger.application.ports.GiftRepository
import com.giftledger.domain.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class GiftRepositoryExposed : GiftRepository {

    override fun create(
        userId: UserId,
        guestId: GuestId,
        isReceived: Boolean,
        amount: Money,
        eventType: String,
        eventBookId: EventBookId?,
        occurredAt: LocalDate,
        note: String?
    ): Gift {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()

        transaction {
            GiftsTable.insert {
                it[GiftsTable.id] = id.toString()
                it[GiftsTable.userId] = userId.value.toString()
                it[GiftsTable.guestId] = guestId.value.toString()
                it[GiftsTable.isReceived] = isReceived
                it[GiftsTable.amount] = amount.amount
                it[GiftsTable.eventType] = eventType
                it[GiftsTable.eventBookId] = eventBookId?.value?.toString()
                it[GiftsTable.occurredAt] = occurredAt
                it[GiftsTable.note] = note
                it[GiftsTable.relatedGiftId] = null
                it[GiftsTable.isReturned] = false
                it[GiftsTable.remindedCount] = 0
                it[GiftsTable.lastRemindedAt] = null
                it[GiftsTable.createdAt] = now
                it[GiftsTable.updatedAt] = now
            }
        }

        return Gift(
            id = GiftId(id),
            userId = userId,
            guestId = guestId,
            isReceived = isReceived,
            amount = amount,
            eventType = eventType,
            eventBookId = eventBookId,
            occurredAt = occurredAt,
            note = note,
            relatedGiftId = null,
            isReturned = false,
            remindedCount = 0,
            lastRemindedAt = null,
            createdAt = now.toInstant(ZoneOffset.UTC),
            updatedAt = now.toInstant(ZoneOffset.UTC)
        )
    }

    override fun findById(userId: UserId, id: GiftId): Gift? = transaction {
        GiftsTable.selectAll()
            .where { (GiftsTable.id eq id.value.toString()) and (GiftsTable.userId eq userId.value.toString()) }
            .map { it.toGift() }
            .singleOrNull()
    }


    override fun listByUserId(userId: UserId): List<Gift> = transaction {
        GiftsTable.selectAll()
            .where { GiftsTable.userId eq userId.value.toString() }
            .orderBy(GiftsTable.occurredAt, SortOrder.DESC)
            .map { it.toGift() }
    }

    override fun listUnreturned(userId: UserId): List<Gift> = transaction {
        GiftsTable.selectAll()
            .where { 
                (GiftsTable.userId eq userId.value.toString()) and 
                (GiftsTable.isReturned eq false) and 
                (GiftsTable.isReceived eq true) 
            }
            .orderBy(GiftsTable.occurredAt, SortOrder.DESC)
            .map { it.toGift() }
    }

    override fun listPendingReceipts(userId: UserId): List<Gift> = listUnreturned(userId)

    override fun linkGifts(userId: UserId, giftId1: GiftId, giftId2: GiftId): Boolean = transaction {
        val gift1 = GiftsTable.selectAll()
            .where { (GiftsTable.id eq giftId1.value.toString()) and (GiftsTable.userId eq userId.value.toString()) }
            .singleOrNull() ?: return@transaction false
        
        val gift2 = GiftsTable.selectAll()
            .where { (GiftsTable.id eq giftId2.value.toString()) and (GiftsTable.userId eq userId.value.toString()) }
            .singleOrNull() ?: return@transaction false

        GiftsTable.update({ GiftsTable.id eq giftId1.value.toString() }) {
            it[relatedGiftId] = giftId2.value.toString()
            it[isReturned] = true
            it[updatedAt] = LocalDateTime.now()
        }

        GiftsTable.update({ GiftsTable.id eq giftId2.value.toString() }) {
            it[relatedGiftId] = giftId1.value.toString()
            it[isReturned] = true
            it[updatedAt] = LocalDateTime.now()
        }

        true
    }

    override fun updateReminder(userId: UserId, id: GiftId, remindedCount: Int): Boolean = transaction {
        val updated = GiftsTable.update({ 
            (GiftsTable.id eq id.value.toString()) and (GiftsTable.userId eq userId.value.toString()) 
        }) {
            it[GiftsTable.remindedCount] = remindedCount
            it[lastRemindedAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    override fun delete(userId: UserId, id: GiftId): Boolean = transaction {
        val deleted = GiftsTable.deleteWhere { 
            (GiftsTable.id eq id.value.toString()) and (GiftsTable.userId eq userId.value.toString()) 
        }
        deleted > 0
    }


    override fun listByUserIdPaginated(
        userId: UserId,
        page: Int,
        pageSize: Int,
        guestId: GuestId?,
        eventBookId: EventBookId?,
        isReceived: Boolean?
    ): Pair<List<Gift>, Int> = transaction {
        var query = GiftsTable.selectAll().where { GiftsTable.userId eq userId.value.toString() }
        
        guestId?.let { g ->
            query = query.andWhere { GiftsTable.guestId eq g.value.toString() }
        }
        eventBookId?.let { e ->
            query = query.andWhere { GiftsTable.eventBookId eq e.value.toString() }
        }
        isReceived?.let { r ->
            query = query.andWhere { GiftsTable.isReceived eq r }
        }

        val total = query.count().toInt()
        val offset = ((page - 1) * pageSize).toLong()
        
        val gifts = query
            .orderBy(GiftsTable.occurredAt, SortOrder.DESC)
            .limit(pageSize)
            .offset(offset)
            .map { it.toGift() }

        Pair(gifts, total)
    }

    override fun update(
        userId: UserId,
        id: GiftId,
        guestId: GuestId?,
        isReceived: Boolean?,
        amount: Money?,
        eventType: String?,
        eventBookId: EventBookId?,
        occurredAt: LocalDate?,
        note: String?
    ): Gift? = transaction {
        val existing = GiftsTable.selectAll()
            .where { (GiftsTable.id eq id.value.toString()) and (GiftsTable.userId eq userId.value.toString()) }
            .singleOrNull() ?: return@transaction null

        GiftsTable.update({ (GiftsTable.id eq id.value.toString()) and (GiftsTable.userId eq userId.value.toString()) }) {
            guestId?.let { g -> it[GiftsTable.guestId] = g.value.toString() }
            isReceived?.let { r -> it[GiftsTable.isReceived] = r }
            amount?.let { a -> it[GiftsTable.amount] = a.amount }
            eventType?.let { e -> it[GiftsTable.eventType] = e }
            eventBookId?.let { e -> it[GiftsTable.eventBookId] = e.value.toString() }
            occurredAt?.let { o -> it[GiftsTable.occurredAt] = o }
            note?.let { n -> it[GiftsTable.note] = n }
            it[updatedAt] = LocalDateTime.now()
        }

        GiftsTable.selectAll()
            .where { GiftsTable.id eq id.value.toString() }
            .map { it.toGift() }
            .singleOrNull()
    }


    override fun countByGuestId(userId: UserId, guestId: GuestId): Int = transaction {
        GiftsTable.selectAll()
            .where { 
                (GiftsTable.userId eq userId.value.toString()) and 
                (GiftsTable.guestId eq guestId.value.toString()) 
            }
            .count()
            .toInt()
    }

    override fun countByEventBookId(userId: UserId, eventBookId: EventBookId): Int = transaction {
        GiftsTable.selectAll()
            .where {
                (GiftsTable.userId eq userId.value.toString()) and
                (GiftsTable.eventBookId eq eventBookId.value.toString())
            }
            .count()
            .toInt()
    }

    override fun listAll(userId: UserId): List<Gift> = transaction {
        GiftsTable.selectAll()
            .where { GiftsTable.userId eq userId.value.toString() }
            .orderBy(GiftsTable.occurredAt, SortOrder.DESC)
            .map { it.toGift() }
    }

    override fun link(userId: UserId, id1: GiftId, id2: GiftId): Boolean =
        linkGifts(userId, id1, id2)

    private fun ResultRow.toGift(): Gift = Gift(
        id = GiftId(UUID.fromString(this[GiftsTable.id])),
        userId = UserId(UUID.fromString(this[GiftsTable.userId])),
        guestId = GuestId(UUID.fromString(this[GiftsTable.guestId])),
        isReceived = this[GiftsTable.isReceived],
        amount = Money(this[GiftsTable.amount]),
        eventType = this[GiftsTable.eventType],
        eventBookId = this[GiftsTable.eventBookId]?.let { EventBookId(UUID.fromString(it)) },
        occurredAt = this[GiftsTable.occurredAt],
        note = this[GiftsTable.note],
        relatedGiftId = this[GiftsTable.relatedGiftId]?.let { GiftId(UUID.fromString(it)) },
        isReturned = this[GiftsTable.isReturned],
        remindedCount = this[GiftsTable.remindedCount],
        lastRemindedAt = this[GiftsTable.lastRemindedAt]?.toInstant(ZoneOffset.UTC),
        createdAt = this[GiftsTable.createdAt].toInstant(ZoneOffset.UTC),
        updatedAt = this[GiftsTable.updatedAt].toInstant(ZoneOffset.UTC)
    )
}
