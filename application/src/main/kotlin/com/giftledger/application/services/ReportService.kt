package com.giftledger.application.services

import com.giftledger.application.ports.ReportRepository
import com.giftledger.domain.UserId
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
data class SummaryReport(
    val totalReceived: Double,
    val totalSent: Double,
    val netBalance: Double,
    val pendingCount: Int,
)

@Serializable
data class GuestTotal(
    val guestId: String,
    val guestName: String,
    val totalReceived: Double,
    val totalSent: Double,
    val netBalance: Double,
    val giftCount: Int,
)

@Serializable
data class EventBookTotal(
    val eventBookId: String,
    val eventBookName: String,
    val totalReceived: Double,
    val totalSent: Double,
    val netBalance: Double,
    val giftCount: Int,
)

class ReportService(private val reportRepository: ReportRepository) {
    fun getSummary(userId: UserId, from: LocalDate? = null, to: LocalDate? = null): SummaryReport {
        return reportRepository.getSummary(userId, from, to)
    }

    fun getGuestTotals(userId: UserId, from: LocalDate? = null, to: LocalDate? = null): List<GuestTotal> {
        return reportRepository.getGuestTotals(userId, from, to)
    }

    fun getEventBookTotals(userId: UserId, from: LocalDate? = null, to: LocalDate? = null): List<EventBookTotal> {
        return reportRepository.getEventBookTotals(userId, from, to)
    }
}
