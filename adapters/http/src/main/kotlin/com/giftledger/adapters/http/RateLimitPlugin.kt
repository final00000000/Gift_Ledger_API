package com.giftledger.adapters.http

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*

/**
 * Ktor 限流插件
 */
val RateLimitPlugin = createApplicationPlugin(name = "RateLimitPlugin") {
    
    onCall { call ->
        val path = call.request.path()
        val ip = RateLimiter.getClientIp(call)
        val fingerprint = RateLimiter.getDeviceFingerprint(call)
        
        // 1. 全局限流检查
        if (!RateLimiter.checkGlobalLimit()) {
            call.respond(HttpStatusCode.ServiceUnavailable, ApiResponse.error(
                503, "服务器繁忙，请稍后重试"
            ))
            return@onCall
        }
        
        // 2. IP限流检查
        if (!RateLimiter.checkIpLimit(ip)) {
            call.respond(HttpStatusCode.TooManyRequests, ApiResponse.error(
                429, "请求过于频繁，请稍后重试"
            ))
            return@onCall
        }
        
        // 3. 设备指纹限流检查
        if (!RateLimiter.checkDeviceLimit(fingerprint)) {
            call.respond(HttpStatusCode.TooManyRequests, ApiResponse.error(
                429, "请求过于频繁，请稍后重试"
            ))
            return@onCall
        }
        
        // 4. 已登录用户的限流检查
        val principal = call.principal<JWTPrincipal>()
        if (principal != null) {
            val userId = principal.payload.getClaim("uid").asString()
            if (!RateLimiter.checkUserLimit(userId)) {
                call.respond(HttpStatusCode.TooManyRequests, ApiResponse.error(
                    429, "请求过于频繁，请稍后重试"
                ))
                return@onCall
            }
        }
    }
}
