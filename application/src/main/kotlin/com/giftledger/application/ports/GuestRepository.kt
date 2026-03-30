package com.giftledger.application.ports

import com.giftledger.domain.Guest
import com.giftledger.domain.GuestId
import com.giftledger.domain.UserId

interface GuestRepository {
    fun create(userId: UserId, name: String, relationship: String, phone: String?, note: String?): Guest
    fun findById(userId: UserId, id: GuestId): Guest?
    fun listByUserId(userId: UserId): List<Guest>
    fun update(userId: UserId, id: GuestId, name: String?, relationship: String?, phone: String?, note: String?): Guest?
    fun delete(userId: UserId, id: GuestId): Boolean

    // 新增：分页查询，支持搜索
    fun listByUserIdPaginated(
        userId: UserId,
        page: Int,
        pageSize: Int,
        search: String? = null
    ): Pair<List<Guest>, Int>
}
