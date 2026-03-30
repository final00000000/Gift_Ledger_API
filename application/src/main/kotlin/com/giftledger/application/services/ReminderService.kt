package com.giftledger.application.services

import com.giftledger.application.ports.GiftRepository
import com.giftledger.application.ports.ReminderSettingsRepository
import com.giftledger.domain.Gift
import com.giftledger.domain.GiftDirection
import com.giftledger.domain.ReminderSettings
import com.giftledger.domain.UserId
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class PendingReminder(
    val giftId: String,
    val guestName: String,
    val amount: Double,
    val eventType: String,
    val occurredAt: String,
    val daysLeft: Int,
    val remindedCount: Int,
    val statusLevel: Int, // 0: early, 1: warning, 2: overdue
)

class ReminderService(
    private val giftRepository: GiftRepository,
    private val reminderSettingsRepository: ReminderSettingsRepository,
) {
    fun getPendingReminders(userId: UserId, type: String = "unreturned"): List<PendingReminder> {
        val settings = reminderSettingsRepository.get(userId)
        val today = LocalDate.now()

        val gifts = when (type) {
            "unreturned" -> giftRepository.listUnreturned(userId)
            "pending-receipts" -> giftRepository.listPendingReceipts(userId)
            else -> giftRepository.listUnreturned(userId)
        }

        return gifts.mapNotNull { gift ->
            val daysPassed = java.time.temporal.ChronoUnit.DAYS.between(gift.occurredAt, today).toInt()
            val daysLeft = settings.dueDays - daysPassed
            val statusLevel = when {
                daysLeft > settings.warnDays -> 0
                daysLeft > 0 -> 1
                else -> 2
            }

            if (gift.remindedCount < settings.maxRemindCount) {
                PendingReminder(
                    giftId = gift.id.value.toString(),
                    guestName = "Guest", // Will be joined with guest name in HTTP layer
                    amount = gift.amount.amount.toDouble(),
                    eventType = gift.eventType,
                    occurredAt = gift.occurredAt.toString(),
                    daysLeft = daysLeft,
                    remindedCount = gift.remindedCount,
                    statusLevel = statusLevel,
                )
            } else {
                null
            }
        }
    }

    fun autoLinkGifts(userId: UserId): Pair<Int, Int> {
        val gifts = giftRepository.listAll(userId)
        var linked = 0
        var skipped = 0

        val received = gifts.filter { it.isReceived && !it.isReturned }
        val sent = gifts.filter { !it.isReceived && !it.isReturned }

        for (r in received) {
            val candidates = sent.filter { s ->
                s.guestId == r.guestId &&
                s.eventType == r.eventType &&
                java.time.temporal.ChronoUnit.DAYS.between(r.occurredAt, s.occurredAt).let { it in -365..365 } &&
                s.relatedGiftId == null
            }

            if (candidates.isNotEmpty()) {
                val s = candidates.first()
                giftRepository.link(userId, r.id, s.id)
                linked++
            } else {
                skipped++
            }
        }

        return Pair(linked, skipped)
    }

    fun getReminderSettings(userId: UserId): ReminderSettings {
        return reminderSettingsRepository.get(userId)
    }

    fun updateReminderSettings(userId: UserId, dueDays: Int, warnDays: Int, maxRemindCount: Int, template: String): ReminderSettings {
        return reminderSettingsRepository.update(userId, dueDays, warnDays, maxRemindCount, template)
    }
}
