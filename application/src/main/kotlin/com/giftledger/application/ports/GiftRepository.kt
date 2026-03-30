package com.giftledger.application.ports

import com.giftledger.domain.EventBookId
import com.giftledger.domain.Gift
import com.giftledger.domain.GiftId
import com.giftledger.domain.GuestId
import com.giftledger.domain.Money
import com.giftledger.domain.UserId
import java.time.Instant
import java.time.LocalDate

interface GiftRepository {
    fun create(
        userId: UserId,
        guestId: GuestId,
        isReceived: Boolean,
        amount: Money,
        eventType: String,
        eventBookId: EventBookId?,
        occurredAt: LocalDate,
        note: String?,
    ): Gift

    fun findById(userId: UserId, id: GiftId): Gift?
    fun listByUserId(userId: UserId): List<Gift>
    fun listUnreturned(userId: UserId): List<Gift>
    fun listPendingReceipts(userId: UserId): List<Gift>
    fun linkGifts(userId: UserId, giftId1: GiftId, giftId2: GiftId): Boolean
    fun updateReminder(userId: UserId, id: GiftId, remindedCount: Int): Boolean
    fun delete(userId: UserId, id: GiftId): Boolean

    // 新增：分页查询，支持筛选
    fun listByUserIdPaginated(
        userId: UserId,
        page: Int,
        pageSize: Int,
        guestId: GuestId? = null,
        eventBookId: EventBookId? = null,
        isReceived: Boolean? = null
    ): Pair<List<Gift>, Int>

    // 新增：更新礼物记录
    fun update(
        userId: UserId,
        id: GiftId,
        guestId: GuestId? = null,
        isReceived: Boolean? = null,
        amount: Money? = null,
        eventType: String? = null,
        eventBookId: EventBookId? = null,
        occurredAt: LocalDate? = null,
        note: String? = null
    ): Gift?

    // 新增：检查宾客是否有关联礼物
    fun countByGuestId(userId: UserId, guestId: GuestId): Int

    // 新增：检查活动账本是否有关联礼物
    fun countByEventBookId(userId: UserId, eventBookId: EventBookId): Int

    // ReminderService 使用：列出所有礼物
    fun listAll(userId: UserId): List<Gift>

    // ReminderService 使用：关联两条礼物记录
    fun link(userId: UserId, id1: GiftId, id2: GiftId): Boolean
}
