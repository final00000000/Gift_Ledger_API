package com.giftledger.adapters.persistence.exposed

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val username = varchar("username", 255)
    val email = varchar("email", 255)
    val fullName = varchar("full_name", 255).nullable()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object RefreshTokensTable : Table("refresh_tokens") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val tokenHash = varchar("token_hash", 255)
    val expiresAt = datetime("expires_at")
    val revokedAt = datetime("revoked_at").nullable()
    val userAgent = text("user_agent").nullable()
    val ip = varchar("ip", 45).nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

object GuestsTable : Table("guests") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val name = varchar("name", 255)
    val relationship = varchar("relationship", 255)
    val phone = varchar("phone", 50).nullable()
    val note = text("note").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object EventBooksTable : Table("event_books") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val name = varchar("name", 255)
    val type = varchar("type", 100)
    val eventDate = date("event_date")
    val lunarDate = varchar("lunar_date", 50).nullable()
    val note = text("note").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}


object GiftsTable : Table("gifts") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val guestId = varchar("guest_id", 36).references(GuestsTable.id)
    val isReceived = bool("is_received")
    val amount = decimal("amount", 12, 2)
    val eventType = varchar("event_type", 100)
    val eventBookId = varchar("event_book_id", 36).references(EventBooksTable.id).nullable()
    val occurredAt = date("occurred_at")
    val note = text("note").nullable()
    val relatedGiftId = varchar("related_gift_id", 36).nullable()
    val isReturned = bool("is_returned")
    val remindedCount = integer("reminded_count")
    val lastRemindedAt = datetime("last_reminded_at").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object ReminderSettingsTable : Table("reminder_settings") {
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val dueDays = integer("due_days")
    val warnDays = integer("warn_days")
    val maxRemindCount = integer("max_remind_count")
    val template = text("template")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(userId)
}
