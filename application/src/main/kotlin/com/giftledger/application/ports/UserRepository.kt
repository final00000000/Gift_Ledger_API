package com.giftledger.application.ports

import com.giftledger.domain.User
import com.giftledger.domain.UserId

interface UserRepository {
    fun create(email: String, passwordHash: String): User
    fun findById(id: UserId): User?
    fun findByEmail(email: String): User?
    fun updatePasswordHash(id: UserId, newPasswordHash: String): Boolean
    fun delete(id: UserId): Boolean

    // 新增：更新用户资料
    fun updateProfile(
        id: UserId,
        username: String? = null,
        email: String? = null,
        fullName: String? = null
    ): User?
}
