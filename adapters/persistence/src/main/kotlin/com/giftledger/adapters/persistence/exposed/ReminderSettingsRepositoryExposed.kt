package com.giftledger.adapters.persistence.exposed

import com.giftledger.application.ports.ReminderSettingsRepository
import com.giftledger.domain.ReminderSettings
import com.giftledger.domain.UserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

class ReminderSettingsRepositoryExposed : ReminderSettingsRepository {

    override fun get(userId: UserId): ReminderSettings = transaction {
        val row = ReminderSettingsTable.selectAll()
            .where { ReminderSettingsTable.userId eq userId.value.toString() }
            .singleOrNull()

        if (row != null) {
            row.toReminderSettings(userId)
        } else {
            // 返回默认设置并持久化
            val now = LocalDateTime.now()
            ReminderSettingsTable.insert {
                it[ReminderSettingsTable.userId] = userId.value.toString()
                it[dueDays] = 180
                it[warnDays] = 90
                it[maxRemindCount] = 3
                it[template] = "你有一笔来自 {name} 的礼金还未回礼，请及时处理。"
                it[createdAt] = now
                it[updatedAt] = now
            }
            ReminderSettings(
                userId = userId,
                dueDays = 180,
                warnDays = 90,
                maxRemindCount = 3,
                template = "你有一笔来自 {name} 的礼金还未回礼，请及时处理。",
                createdAt = now.toInstant(ZoneOffset.UTC),
                updatedAt = now.toInstant(ZoneOffset.UTC),
            )
        }
    }

    override fun update(
        userId: UserId,
        dueDays: Int,
        warnDays: Int,
        maxRemindCount: Int,
        template: String,
    ): ReminderSettings = transaction {
        val now = LocalDateTime.now()
        val existing = ReminderSettingsTable.selectAll()
            .where { ReminderSettingsTable.userId eq userId.value.toString() }
            .singleOrNull()

        if (existing != null) {
            ReminderSettingsTable.update({ ReminderSettingsTable.userId eq userId.value.toString() }) {
                it[ReminderSettingsTable.dueDays] = dueDays
                it[ReminderSettingsTable.warnDays] = warnDays
                it[ReminderSettingsTable.maxRemindCount] = maxRemindCount
                it[ReminderSettingsTable.template] = template
                it[updatedAt] = now
            }
        } else {
            ReminderSettingsTable.insert {
                it[ReminderSettingsTable.userId] = userId.value.toString()
                it[ReminderSettingsTable.dueDays] = dueDays
                it[ReminderSettingsTable.warnDays] = warnDays
                it[ReminderSettingsTable.maxRemindCount] = maxRemindCount
                it[ReminderSettingsTable.template] = template
                it[createdAt] = now
                it[updatedAt] = now
            }
        }

        ReminderSettingsTable.selectAll()
            .where { ReminderSettingsTable.userId eq userId.value.toString() }
            .single()
            .toReminderSettings(userId)
    }

    private fun ResultRow.toReminderSettings(userId: UserId): ReminderSettings = ReminderSettings(
        userId = userId,
        dueDays = this[ReminderSettingsTable.dueDays],
        warnDays = this[ReminderSettingsTable.warnDays],
        maxRemindCount = this[ReminderSettingsTable.maxRemindCount],
        template = this[ReminderSettingsTable.template],
        createdAt = this[ReminderSettingsTable.createdAt].toInstant(ZoneOffset.UTC),
        updatedAt = this[ReminderSettingsTable.updatedAt].toInstant(ZoneOffset.UTC),
    )
}
