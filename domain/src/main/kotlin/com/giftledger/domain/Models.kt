package com.giftledger.domain

import java.time.Instant
import java.util.UUID

@JvmInline value class UserId(val value: UUID)
@JvmInline value class GuestId(val value: UUID)
@JvmInline value class EventBookId(val value: UUID)
@JvmInline value class GiftId(val value: UUID)

@JvmInline
value class Money(val amount: java.math.BigDecimal) {
    init { require(amount > java.math.BigDecimal.ZERO) }
}

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val fullName: String?,
    val passwordHash: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class Guest(
    val id: GuestId,
    val userId: UserId,
    val name: String,
    val relationship: String,
    val phone: String?,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class EventBook(
    val id: EventBookId,
    val userId: UserId,
    val name: String,
    val type: String,
    val eventDate: java.time.LocalDate,
    val lunarDate: String?,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class GiftDirection { RECEIVED, SENT }

data class Gift(
    val id: GiftId,
    val userId: UserId,
    val guestId: GuestId,
    val isReceived: Boolean,
    val amount: Money,
    val eventType: String,
    val eventBookId: EventBookId?,
    val occurredAt: java.time.LocalDate,
    val note: String?,
    val relatedGiftId: GiftId?,
    val isReturned: Boolean,
    val remindedCount: Int,
    val lastRemindedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ReminderSettings(
    val userId: UserId,
    val dueDays: Int,
    val warnDays: Int,
    val maxRemindCount: Int,
    val template: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
