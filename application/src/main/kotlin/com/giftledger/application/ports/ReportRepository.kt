package com.giftledger.application.ports

import com.giftledger.application.services.EventBookTotal
import com.giftledger.application.services.GuestTotal
import com.giftledger.application.services.SummaryReport
import com.giftledger.domain.UserId
import java.time.LocalDate

interface ReportRepository {
    fun getSummary(userId: UserId, from: LocalDate? = null, to: LocalDate? = null): SummaryReport
    fun getGuestTotals(userId: UserId, from: LocalDate? = null, to: LocalDate? = null): List<GuestTotal>
    fun getEventBookTotals(userId: UserId, from: LocalDate? = null, to: LocalDate? = null): List<EventBookTotal>
}
