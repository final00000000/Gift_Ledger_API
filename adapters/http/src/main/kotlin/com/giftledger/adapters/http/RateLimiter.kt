package com.giftledger.adapters.http

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * 多层限流器
 * 1. IP限流 - 防止单IP刷接口
 * 2. 账号限流 - 防止单账号刷接口
 * 3. 登录失败锁定 - 防止暴力破解
 * 4. 全局限流 - 防止DDoS
 */
object RateLimiter {
    
    // IP请求记录: IP -> (时间戳列表)
    private val ipRequests = ConcurrentHashMap<String, MutableList<Long>>()
    
    // 用户请求记录: userId -> (时间戳列表)
    private val userRequests = ConcurrentHashMap<String, MutableList<Long>>()
    
    // 登录失败记录: email -> (失败次数, 最后失败时间)
    private val loginFailures = ConcurrentHashMap<String, Pair<Int, Long>>()
    
    // 账号锁定记录: email -> 解锁时间
    private val accountLocks = ConcurrentHashMap<String, Long>()
    
    // 全局请求计数: 秒级时间戳 -> 请求数
    private val globalRequests = ConcurrentHashMap<Long, Int>()
    
    // 设备指纹记录: fingerprint -> (时间戳列表)
    private val deviceRequests = ConcurrentHashMap<String, MutableList<Long>>()
    
    // 配置参数
    object Config {
        const val IP_LIMIT_PER_MINUTE = 60          // 每IP每分钟最多60次
        const val USER_LIMIT_PER_MINUTE = 100       // 每用户每分钟最多100次
        const val LOGIN_MAX_FAILURES = 5            // 登录最多失败5次
        const val ACCOUNT_LOCK_MINUTES = 15L        // 账号锁定15分钟
        const val GLOBAL_LIMIT_PER_SECOND = 1000    // 全局每秒最多1000次
        const val DEVICE_LIMIT_PER_MINUTE = 30      // 每设备每分钟最多30次
        const val CLEANUP_INTERVAL_MS = 60000L      // 清理间隔1分钟
    }

    
    private var lastCleanup = System.currentTimeMillis()
    
    /**
     * 获取客户端IP
     */
    fun getClientIp(call: ApplicationCall): String {
        return call.request.headers["X-Forwarded-For"]?.split(",")?.firstOrNull()?.trim()
            ?: call.request.headers["X-Real-IP"]
            ?: call.request.origin.remoteAddress
    }
    
    /**
     * 生成设备指纹
     */
    fun getDeviceFingerprint(call: ApplicationCall): String {
        val userAgent = call.request.headers["User-Agent"] ?: "unknown"
        val acceptLang = call.request.headers["Accept-Language"] ?: ""
        val acceptEnc = call.request.headers["Accept-Encoding"] ?: ""
        return "$userAgent|$acceptLang|$acceptEnc".hashCode().toString()
    }
    
    /**
     * 检查IP限流
     */
    fun checkIpLimit(ip: String): Boolean {
        cleanupIfNeeded()
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - 60000
        
        val requests = ipRequests.getOrPut(ip) { mutableListOf() }
        synchronized(requests) {
            requests.removeIf { it < oneMinuteAgo }
            if (requests.size >= Config.IP_LIMIT_PER_MINUTE) {
                return false
            }
            requests.add(now)
        }
        return true
    }
    
    /**
     * 检查用户限流
     */
    fun checkUserLimit(userId: String): Boolean {
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - 60000
        
        val requests = userRequests.getOrPut(userId) { mutableListOf() }
        synchronized(requests) {
            requests.removeIf { it < oneMinuteAgo }
            if (requests.size >= Config.USER_LIMIT_PER_MINUTE) {
                return false
            }
            requests.add(now)
        }
        return true
    }
    
    /**
     * 检查设备限流
     */
    fun checkDeviceLimit(fingerprint: String): Boolean {
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - 60000
        
        val requests = deviceRequests.getOrPut(fingerprint) { mutableListOf() }
        synchronized(requests) {
            requests.removeIf { it < oneMinuteAgo }
            if (requests.size >= Config.DEVICE_LIMIT_PER_MINUTE) {
                return false
            }
            requests.add(now)
        }
        return true
    }

    
    /**
     * 检查全局限流
     */
    fun checkGlobalLimit(): Boolean {
        val now = System.currentTimeMillis() / 1000
        val count = globalRequests.compute(now) { _, v -> (v ?: 0) + 1 } ?: 1
        // 清理旧数据
        globalRequests.keys.removeIf { it < now - 5 }
        return count <= Config.GLOBAL_LIMIT_PER_SECOND
    }
    
    /**
     * 检查账号是否被锁定
     */
    fun isAccountLocked(email: String): Boolean {
        val unlockTime = accountLocks[email] ?: return false
        if (System.currentTimeMillis() > unlockTime) {
            accountLocks.remove(email)
            loginFailures.remove(email)
            return false
        }
        return true
    }
    
    /**
     * 获取账号解锁剩余时间（秒）
     */
    fun getAccountLockRemainingSeconds(email: String): Long {
        val unlockTime = accountLocks[email] ?: return 0
        val remaining = (unlockTime - System.currentTimeMillis()) / 1000
        return if (remaining > 0) remaining else 0
    }
    
    /**
     * 记录登录失败
     */
    fun recordLoginFailure(email: String) {
        val now = System.currentTimeMillis()
        val (failures, _) = loginFailures.getOrDefault(email, Pair(0, now))
        val newFailures = failures + 1
        loginFailures[email] = Pair(newFailures, now)
        
        if (newFailures >= Config.LOGIN_MAX_FAILURES) {
            val unlockTime = now + Config.ACCOUNT_LOCK_MINUTES * 60 * 1000
            accountLocks[email] = unlockTime
        }
    }
    
    /**
     * 清除登录失败记录（登录成功时调用）
     */
    fun clearLoginFailures(email: String) {
        loginFailures.remove(email)
        accountLocks.remove(email)
    }
    
    /**
     * 获取登录失败次数
     */
    fun getLoginFailureCount(email: String): Int {
        return loginFailures[email]?.first ?: 0
    }
    
    /**
     * 定期清理过期数据
     */
    private fun cleanupIfNeeded() {
        val now = System.currentTimeMillis()
        if (now - lastCleanup < Config.CLEANUP_INTERVAL_MS) return
        lastCleanup = now
        
        val oneMinuteAgo = now - 60000
        
        // 清理IP记录
        ipRequests.entries.removeIf { (_, list) ->
            synchronized(list) { list.removeIf { it < oneMinuteAgo }; list.isEmpty() }
        }
        
        // 清理用户记录
        userRequests.entries.removeIf { (_, list) ->
            synchronized(list) { list.removeIf { it < oneMinuteAgo }; list.isEmpty() }
        }
        
        // 清理设备记录
        deviceRequests.entries.removeIf { (_, list) ->
            synchronized(list) { list.removeIf { it < oneMinuteAgo }; list.isEmpty() }
        }
        
        // 清理过期的登录失败记录（超过锁定时间的）
        val lockExpireTime = now - Config.ACCOUNT_LOCK_MINUTES * 60 * 1000
        loginFailures.entries.removeIf { (_, pair) -> pair.second < lockExpireTime }
        accountLocks.entries.removeIf { (_, unlockTime) -> now > unlockTime }
    }
}
