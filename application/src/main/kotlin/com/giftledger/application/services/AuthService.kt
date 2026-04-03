package com.giftledger.application.services

import com.giftledger.application.ports.RefreshTokenRepository
import com.giftledger.application.ports.UserRepository
import com.giftledger.domain.User
import com.giftledger.domain.UserId
import kotlinx.serialization.Serializable
import java.time.Instant
import java.security.MessageDigest

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)

@Serializable
data class AuthUser(
    val id: String,
    val email: String,
)

interface PasswordHasher {
    fun hash(password: String): String
    fun verify(password: String, hash: String): Boolean
}

interface JwtTokenProvider {
    fun generateAccessToken(userId: String): String
    fun generateRefreshToken(userId: String): String
}

class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProvider: JwtTokenProvider,
    private val passwordHasher: PasswordHasher,
) {
    private fun accessTokenTtlSeconds(env: Map<String, String> = System.getenv()): Long =
        env["JWT_ACCESS_TTL_SECONDS"]?.trim()?.toLongOrNull()?.takeIf { it > 0 } ?: 15 * 60L

    private fun refreshTokenTtlSeconds(env: Map<String, String> = System.getenv()): Long =
        env["JWT_REFRESH_TTL_SECONDS"]?.trim()?.toLongOrNull()?.takeIf { it > 0 } ?: 30L * 24 * 60 * 60

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { b -> "%02x".format(b) }
    }

    fun register(email: String, password: String, username: String? = null, fullName: String? = null): AuthUser {
        val normalizedEmail = email.trim().lowercase()
        val normalizedUsername = username?.trim().orEmpty()

        if (normalizedUsername.isBlank()) {
            throw IllegalArgumentException("用户名不能为空")
        }
        if (normalizedUsername.length < 2) {
            throw IllegalArgumentException("用户名至少2个字符")
        }

        val existingEmail = userRepository.findByEmail(normalizedEmail)
        if (existingEmail != null) throw IllegalArgumentException("邮箱已注册")

        val existingUsername = userRepository.findByUsername(normalizedUsername)
        if (existingUsername != null) throw IllegalArgumentException("用户名已存在")

        val passwordHash = passwordHasher.hash(password)
        val user = userRepository.create(normalizedEmail, passwordHash, normalizedUsername, fullName?.trim())
        return AuthUser(user.id.value.toString(), user.email)
    }

    fun login(email: String, password: String, userAgent: String? = null, ip: String? = null): AuthTokens {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("凭证无效")

        if (!passwordHasher.verify(password, user.passwordHash)) {
            throw IllegalArgumentException("凭证无效")
        }

        return generateTokens(user, userAgent, ip)
    }

    fun refreshAccessToken(refreshTokenStr: String): AuthTokens {
        val tokenHash = sha256Hex(refreshTokenStr)
        val token = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw IllegalArgumentException("刷新令牌无效")

        if (token.revokedAt != null) throw IllegalArgumentException("令牌已撤销")
        if (Instant.now().isAfter(token.expiresAt)) throw IllegalArgumentException("令牌已过期")

        val user = userRepository.findById(token.userId)
            ?: throw IllegalArgumentException("用户不存在")

        return generateTokens(user, null, null)
    }

    fun logout(refreshTokenStr: String) {
        val tokenHash = sha256Hex(refreshTokenStr)
        val token = refreshTokenRepository.findByTokenHash(tokenHash)
        if (token != null) {
            refreshTokenRepository.revoke(token.id)
        }
    }

    private fun generateTokens(user: User, userAgent: String?, ip: String?): AuthTokens {
        val accessTtlSeconds = accessTokenTtlSeconds()
        val refreshTtlSeconds = refreshTokenTtlSeconds()

        val accessToken = jwtProvider.generateAccessToken(user.id.value.toString())
        val refreshTokenStr = jwtProvider.generateRefreshToken(user.id.value.toString())
        val refreshTokenHash = sha256Hex(refreshTokenStr)
        val expiresAt = Instant.now().plusSeconds(refreshTtlSeconds)

        refreshTokenRepository.create(user.id, refreshTokenHash, expiresAt, userAgent, ip)

        return AuthTokens(accessToken, refreshTokenStr, accessTtlSeconds)
    }
}

class GuestService(private val guestRepository: com.giftledger.application.ports.GuestRepository) {
    fun createGuest(userId: UserId, name: String, relationship: String, phone: String?, note: String?) =
        guestRepository.create(userId, name, relationship, phone, note)

    fun getGuest(userId: UserId, guestId: com.giftledger.domain.GuestId) =
        guestRepository.findById(userId, guestId)

    fun listGuests(userId: UserId) = guestRepository.listByUserId(userId)

    fun updateGuest(userId: UserId, guestId: com.giftledger.domain.GuestId, name: String, relationship: String, phone: String?, note: String?) =
        guestRepository.update(userId, guestId, name, relationship, phone, note)

    fun deleteGuest(userId: UserId, guestId: com.giftledger.domain.GuestId) =
        guestRepository.delete(userId, guestId)
}

class EventBookService(private val eventBookRepository: com.giftledger.application.ports.EventBookRepository) {
    fun createEventBook(userId: UserId, name: String, type: String, eventDate: java.time.LocalDate, lunarDate: String?, note: String?) =
        eventBookRepository.create(userId, name, type, eventDate, lunarDate, note)

    fun getEventBook(userId: UserId, eventBookId: com.giftledger.domain.EventBookId) =
        eventBookRepository.findById(userId, eventBookId)

    fun listEventBooks(userId: UserId) = eventBookRepository.listByUserId(userId)

    fun updateEventBook(userId: UserId, eventBookId: com.giftledger.domain.EventBookId, name: String, type: String, eventDate: java.time.LocalDate, lunarDate: String?, note: String?) =
        eventBookRepository.update(userId, eventBookId, name, type, eventDate, lunarDate, note)

    fun deleteEventBook(userId: UserId, eventBookId: com.giftledger.domain.EventBookId) =
        eventBookRepository.delete(userId, eventBookId)
}

class GiftService(private val giftRepository: com.giftledger.application.ports.GiftRepository) {
    fun createGift(userId: UserId, guestId: com.giftledger.domain.GuestId, isReceived: Boolean, amount: com.giftledger.domain.Money, eventType: String, eventBookId: com.giftledger.domain.EventBookId?, occurredAt: java.time.LocalDate, note: String?) =
        giftRepository.create(userId, guestId, isReceived, amount, eventType, eventBookId, occurredAt, note)

    fun getGift(userId: UserId, giftId: com.giftledger.domain.GiftId) =
        giftRepository.findById(userId, giftId)

    fun listGifts(userId: UserId) = giftRepository.listByUserId(userId)

    fun linkGifts(userId: UserId, giftId1: com.giftledger.domain.GiftId, giftId2: com.giftledger.domain.GiftId) =
        giftRepository.linkGifts(userId, giftId1, giftId2)

    fun deleteGift(userId: UserId, giftId: com.giftledger.domain.GiftId) =
        giftRepository.delete(userId, giftId)
}
