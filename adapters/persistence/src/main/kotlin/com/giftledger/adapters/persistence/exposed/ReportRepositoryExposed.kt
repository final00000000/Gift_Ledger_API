package com.giftledger.adapters.persistence.exposed

import com.giftledger.application.ports.ReportRepository
import com.giftledger.application.services.EventBookTotal
import com.giftledger.application.services.GuestTotal
import com.giftledger.application.services.SummaryReport
import com.giftledger.domain.UserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

class ReportRepositoryExposed : ReportRepository {

    override fun getSummary(userId: UserId, from: LocalDate?, to: LocalDate?): SummaryReport = transaction {
        val uid = userId.value.toString()
        var query = GiftsTable.selectAll().where { GiftsTable.userId eq uid }
        if (from != null) query = query.andWhere { GiftsTable.occurredAt greaterEq from }
        if (to != null) query = query.andWhere { GiftsTable.occurredAt lessEq to }

        var totalReceived = 0.0
        var totalSent = 0.0
        var pendingCount = 0

        query.forEach { row ->
            val amount = row[GiftsTable.amount].toDouble()
            if (row[GiftsTable.isReceived]) {
                totalReceived += amount
                if (!row[GiftsTable.isReturned]) pendingCount++
            } else {
                totalSent += amount
            }
        }

        SummaryReport(
            totalReceived = totalReceived,
            totalSent = totalSent,
            netBalance = totalReceived - totalSent,
            pendingCount = pendingCount,
        )
    }

    override fun getGuestTotals(userId: UserId, from: LocalDate?, to: LocalDate?): List<GuestTotal> = transaction {
        val uid = userId.value.toString()

        // 查询该用户所有 guest
        val guests = GuestsTable.selectAll()
            .where { GuestsTable.userId eq uid }
            .associate { it[GuestsTable.id] to it[GuestsTable.name] }

        var giftQuery = GiftsTable.selectAll().where { GiftsTable.userId eq uid }
        if (from != null) giftQuery = giftQuery.andWhere { GiftsTable.occurredAt greaterEq from }
        if (to != null) giftQuery = giftQuery.andWhere { GiftsTable.occurredAt lessEq to }

        // 按 guestId 聚合
        data class Acc(var received: Double = 0.0, var sent: Double = 0.0, var count: Int = 0)
        val acc = mutableMapOf<String, Acc>()

        giftQuery.forEach { row ->
            val gid = row[GiftsTable.guestId]
            val entry = acc.getOrPut(gid) { Acc() }
            val amount = row[GiftsTable.amount].toDouble()
            if (row[GiftsTable.isReceived]) entry.received += amount else entry.sent += amount
            entry.count++
        }

        acc.map { (gid, a) ->
            GuestTotal(
                guestId = gid,
                guestName = guests[gid] ?: "Unknown",
                totalReceived = a.received,
                totalSent = a.sent,
                netBalance = a.received - a.sent,
                giftCount = a.count,
            )
        }.sortedByDescending { it.giftCount }
    }

    override fun getEventBookTotals(userId: UserId, from: LocalDate?, to: LocalDate?): List<EventBookTotal> = transaction {
        val uid = userId.value.toString()

        val eventBooks = EventBooksTable.selectAll()
            .where { EventBooksTable.userId eq uid }
            .associate { it[EventBooksTable.id] to it[EventBooksTable.name] }

        var giftQuery = GiftsTable.selectAll()
            .where { (GiftsTable.userId eq uid) and (GiftsTable.eventBookId.isNotNull()) }
        if (from != null) giftQuery = giftQuery.andWhere { GiftsTable.occurredAt greaterEq from }
        if (to != null) giftQuery = giftQuery.andWhere { GiftsTable.occurredAt lessEq to }

        data class Acc(var received: Double = 0.0, var sent: Double = 0.0, var count: Int = 0)
        val acc = mutableMapOf<String, Acc>()

        giftQuery.forEach { row ->
            val ebId = row[GiftsTable.eventBookId] ?: return@forEach
            val entry = acc.getOrPut(ebId) { Acc() }
            val amount = row[GiftsTable.amount].toDouble()
            if (row[GiftsTable.isReceived]) entry.received += amount else entry.sent += amount
            entry.count++
        }

        acc.map { (ebId, a) ->
            EventBookTotal(
                eventBookId = ebId,
                eventBookName = eventBooks[ebId] ?: "Unknown",
                totalReceived = a.received,
                totalSent = a.sent,
                netBalance = a.received - a.sent,
                giftCount = a.count,
            )
        }.sortedByDescending { it.giftCount }
    }
}
