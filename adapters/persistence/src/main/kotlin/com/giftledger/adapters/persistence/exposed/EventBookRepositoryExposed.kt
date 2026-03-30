package com.giftledger.adapters.persistence.exposed

import com.giftledger.application.ports.EventBookRepository
import com.giftledger.domain.EventBook
import com.giftledger.domain.EventBookId
import com.giftledger.domain.UserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class EventBookRepositoryExposed : EventBookRepository {

    override fun create(userId: UserId, name: String, type: String, eventDate: LocalDate, lunarDate: String?, note: String?): EventBook {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()

        transaction {
            EventBooksTable.insert {
                it[EventBooksTable.id] = id.toString()
                it[EventBooksTable.userId] = userId.value.toString()
                it[EventBooksTable.name] = name
                it[EventBooksTable.type] = type
                it[EventBooksTable.eventDate] = eventDate
                it[EventBooksTable.lunarDate] = lunarDate
                it[EventBooksTable.note] = note
                it[EventBooksTable.createdAt] = now
                it[EventBooksTable.updatedAt] = now
            }
        }

        return EventBook(
            id = EventBookId(id),
            userId = userId,
            name = name,
            type = type,
            eventDate = eventDate,
            lunarDate = lunarDate,
            note = note,
            createdAt = now.toInstant(ZoneOffset.UTC),
            updatedAt = now.toInstant(ZoneOffset.UTC)
        )
    }

    override fun findById(userId: UserId, id: EventBookId): EventBook? = transaction {
        EventBooksTable.selectAll()
            .where { (EventBooksTable.id eq id.value.toString()) and (EventBooksTable.userId eq userId.value.toString()) }
            .map { it.toEventBook() }
            .singleOrNull()
    }


    override fun listByUserId(userId: UserId): List<EventBook> = transaction {
        EventBooksTable.selectAll()
            .where { EventBooksTable.userId eq userId.value.toString() }
            .orderBy(EventBooksTable.eventDate, SortOrder.DESC)
            .map { it.toEventBook() }
    }

    override fun update(userId: UserId, id: EventBookId, name: String, type: String, eventDate: LocalDate, lunarDate: String?, note: String?): Boolean = transaction {
        val updated = EventBooksTable.update({ 
            (EventBooksTable.id eq id.value.toString()) and (EventBooksTable.userId eq userId.value.toString()) 
        }) {
            it[EventBooksTable.name] = name
            it[EventBooksTable.type] = type
            it[EventBooksTable.eventDate] = eventDate
            it[EventBooksTable.lunarDate] = lunarDate
            it[EventBooksTable.note] = note
            it[updatedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    override fun delete(userId: UserId, id: EventBookId): Boolean = transaction {
        val deleted = EventBooksTable.deleteWhere { 
            (EventBooksTable.id eq id.value.toString()) and (EventBooksTable.userId eq userId.value.toString()) 
        }
        deleted > 0
    }

    private fun ResultRow.toEventBook(): EventBook = EventBook(
        id = EventBookId(UUID.fromString(this[EventBooksTable.id])),
        userId = UserId(UUID.fromString(this[EventBooksTable.userId])),
        name = this[EventBooksTable.name],
        type = this[EventBooksTable.type],
        eventDate = this[EventBooksTable.eventDate],
        lunarDate = this[EventBooksTable.lunarDate],
        note = this[EventBooksTable.note],
        createdAt = this[EventBooksTable.createdAt].toInstant(ZoneOffset.UTC),
        updatedAt = this[EventBooksTable.updatedAt].toInstant(ZoneOffset.UTC)
    )
}
