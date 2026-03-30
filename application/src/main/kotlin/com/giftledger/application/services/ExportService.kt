package com.giftledger.application.services

import com.giftledger.application.ports.EventBookRepository
import com.giftledger.application.ports.GiftRepository
import com.giftledger.application.ports.GuestRepository
import com.giftledger.domain.UserId
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

@Serializable
data class ExportGuestDto(
    val id: String,
    val name: String,
    val relationship: String,
    val phone: String? = null,
    val note: String? = null,
)

@Serializable
data class ExportEventBookDto(
    val id: String,
    val name: String,
    val type: String,
    val eventDate: String,
    val lunarDate: String? = null,
    val note: String? = null,
)

@Serializable
data class ExportGiftDto(
    val id: String,
    val guestId: String,
    val amount: Double,
    val isReceived: Boolean,
    val eventType: String,
    val eventBookId: String? = null,
    val occurredAt: String,
    val note: String? = null,
    val isReturned: Boolean,
    val remindedCount: Int,
)

@Serializable
data class ExportData(
    val version: String = "1.0",
    val exportDate: String,
    val guests: List<ExportGuestDto>,
    val eventBooks: List<ExportEventBookDto>,
    val gifts: List<ExportGiftDto>,
)

class ExportService(
    private val guestRepository: GuestRepository,
    private val eventBookRepository: EventBookRepository,
    private val giftRepository: GiftRepository,
) {
    fun exportJson(userId: UserId): String {
        val guests = guestRepository.listAll(userId).map {
            ExportGuestDto(
                id = it.id.value.toString(),
                name = it.name,
                relationship = it.relationship,
                phone = it.phone,
                note = it.note,
            )
        }

        val eventBooks = eventBookRepository.listAll(userId).map {
            ExportEventBookDto(
                id = it.id.value.toString(),
                name = it.name,
                type = it.type,
                eventDate = it.eventDate.toString(),
                lunarDate = it.lunarDate,
                note = it.note,
            )
        }

        val gifts = giftRepository.listAll(userId).map {
            ExportGiftDto(
                id = it.id.value.toString(),
                guestId = it.guestId.value.toString(),
                amount = it.amount.amount.toDouble(),
                isReceived = it.isReceived,
                eventType = it.eventType,
                eventBookId = it.eventBookId?.value?.toString(),
                occurredAt = it.occurredAt.toString(),
                note = it.note,
                isReturned = it.isReturned,
                remindedCount = it.remindedCount,
            )
        }

        val data = ExportData(
            exportDate = LocalDate.now().toString(),
            guests = guests,
            eventBooks = eventBooks,
            gifts = gifts,
        )

        return Json.encodeToString(data)
    }

    fun exportExcelBytes(userId: UserId): ByteArray {
        // Minimal Excel export: return CSV format as bytes
        val gifts = giftRepository.listAll(userId)
        val csv = buildString {
            appendLine("ID,Guest ID,Amount,Is Received,Event Type,Event Book ID,Occurred At,Note,Is Returned,Reminded Count")
            gifts.forEach { gift ->
                appendLine(
                    "${gift.id.value}," +
                    "${gift.guestId.value}," +
                    "${gift.amount.amount}," +
                    "${gift.isReceived}," +
                    "${gift.eventType}," +
                    "${gift.eventBookId?.value ?: ""}," +
                    "${gift.occurredAt}," +
                    "\"${gift.note ?: ""}\"," +
                    "${gift.isReturned}," +
                    "${gift.remindedCount}"
                )
            }
        }
        return csv.toByteArray(Charsets.UTF_8)
    }

    fun exportPendingExcelBytes(userId: UserId): ByteArray {
        val gifts = giftRepository.listUnreturned(userId)
        val csv = buildString {
            appendLine("ID,Guest ID,Amount,Event Type,Occurred At,Days Overdue,Reminded Count")
            gifts.forEach { gift ->
                val daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(gift.occurredAt, LocalDate.now()).toInt() - 180
                appendLine(
                    "${gift.id.value}," +
                    "${gift.guestId.value}," +
                    "${gift.amount.amount}," +
                    "${gift.eventType}," +
                    "${gift.occurredAt}," +
                    "$daysOverdue," +
                    "${gift.remindedCount}"
                )
            }
        }
        return csv.toByteArray(Charsets.UTF_8)
    }
}
