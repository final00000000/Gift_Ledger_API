package com.giftledger.adapters.http.routes

import com.giftledger.adapters.http.ApiResponse
import com.giftledger.adapters.http.RateLimiter
import com.giftledger.application.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.HttpStatusCode
import org.koin.ktor.ext.inject
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val fullName: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

@Serializable
data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

@Serializable
data class UserRegisterResponse(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String?,
    val createdAt: String
)


fun Application.authRoutes() {
    val authService: AuthService by inject()

    routing {
        post("/auth/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val authUser = authService.register(request.email, request.password, request.username, request.fullName)
                val response = UserRegisterResponse(
                    id = authUser.id,
                    username = request.username,
                    email = authUser.email,
                    fullName = request.fullName,
                    createdAt = java.time.Instant.now().toString()
                )
                call.respond(HttpStatusCode.Created, ApiResponse.created(response, "注册成功"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "注册失败"))
            }
        }

        post("/auth/login") {
            try {
                val request = call.receive<LoginRequest>()
                
                // 检查账号是否被锁定
                if (RateLimiter.isAccountLocked(request.email)) {
                    val remaining = RateLimiter.getAccountLockRemainingSeconds(request.email)
                    call.respond(HttpStatusCode.TooManyRequests, ApiResponse.error(
                        429, "账号已被锁定，请${remaining}秒后重试"
                    ))
                    return@post
                }
                
                val userAgent = call.request.headers["User-Agent"]
                val ip = call.request.headers["X-Forwarded-For"] ?: call.request.local.remoteAddress
                
                try {
                    val tokens = authService.login(request.email, request.password, userAgent, ip)
                    // 登录成功，清除失败记录
                    RateLimiter.clearLoginFailures(request.email)
                    val response = AuthTokenResponse(
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken,
                        expiresIn = tokens.expiresIn
                    )
                    call.respond(ApiResponse.success(response, "登录成功"))
                } catch (e: Exception) {
                    // 登录失败，记录失败次数
                    RateLimiter.recordLoginFailure(request.email)
                    val failures = RateLimiter.getLoginFailureCount(request.email)
                    val remaining = RateLimiter.Config.LOGIN_MAX_FAILURES - failures
                    
                    if (remaining > 0) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(
                            "邮箱或密码错误，还剩${remaining}次尝试机会"
                        ))
                    } else {
                        val lockSeconds = RateLimiter.getAccountLockRemainingSeconds(request.email)
                        call.respond(HttpStatusCode.TooManyRequests, ApiResponse.error(
                            429, "登录失败次数过多，账号已被锁定${lockSeconds}秒"
                        ))
                    }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "登录失败"))
            }
        }

        post("/auth/refresh") {
            try {
                val request = call.receive<RefreshRequest>()
                val tokens = authService.refreshAccessToken(request.refreshToken)
                val response = AuthTokenResponse(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    expiresIn = tokens.expiresIn
                )
                call.respond(ApiResponse.success(response, "刷新成功"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "刷新失败"))
            }
        }

        post("/auth/logout") {
            try {
                val refreshToken = call.receive<LogoutRequest>().refreshToken
                authService.logout(refreshToken)
                call.respond(ApiResponse.success(null, "登出成功"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "登出失败"))
            }
        }
    }
}
