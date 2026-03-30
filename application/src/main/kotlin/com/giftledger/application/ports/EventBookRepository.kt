package com.giftledger.application.ports

import com.giftledger.domain.EventBook
import com.giftledger.domain.EventBookId
import com.giftledger.domain.UserId
import java.time.LocalDate

interface EventBookRepository {
    fun create(userId: UserId, name: String, type: String, eventDate: LocalDate, lunarDate: String?, note: String?): EventBook
    fun findById(userId: UserId, id: EventBookId): EventBook?
    fun listByUserId(userId: UserId): List<EventBook>
    fun update(userId: UserId, id: EventBookId, name: String, type: String, eventDate: LocalDate, lunarDate: String?, note: String?): Boolean
    fun delete(userId: UserId, id: EventBookId): Boolean
}
