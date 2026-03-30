package com.giftledger.adapters.http.routes

import com.giftledger.adapters.exceptions.*
import com.giftledger.adapters.http.ApiResponse
import com.giftledger.adapters.validation.ValidationUtils
import com.giftledger.application.ports.UserRepository
import com.giftledger.application.services.PasswordHasher
import com.giftledger.domain.User
import com.giftledger.domain.UserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.*

@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    val email: String? = null,
    val fullName: String? = null
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class UserProfileResponse(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String?,
    val createdAt: String
)

private fun User.toProfileResponse() = UserProfileResponse(
    id = id.value.toString(),
    username = username,
    email = email,
    fullName = fullName,
    createdAt = createdAt.toString()
)


fun Application.userRoutes() {
    val userRepository: UserRepository by inject()
    val passwordHasher: PasswordHasher by inject()

    routing {
        route("/users") {
            authenticate {
                // GET /users/me - 获取当前用户完整资料
                get("/me") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))

                        val user = userRepository.findById(userId)
                        if (user == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("用户不存在"))
                            return@get
                        }
                        call.respond(ApiResponse.success(user.toProfileResponse()))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "查询失败"))
                    }
                }

                // PUT /users/me - 更新用户资料
                put("/me") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val request = call.receive<UpdateProfileRequest>()

                        val errors = mutableMapOf<String, String>()
                        if (request.username != null && request.username.isBlank()) {
                            errors["username"] = "用户名不能为空"
                        }
                        if (request.email != null) {
                            if (request.email.isBlank()) {
                                errors["email"] = "邮箱不能为空"
                            } else if (!ValidationUtils.validateEmail(request.email)) {
                                errors["email"] = "邮箱格式无效"
                            }
                        }

                        if (errors.isNotEmpty()) {
                            val errorMsg = errors.values.joinToString("; ")
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(errorMsg))
                            return@put
                        }

                        val updated = userRepository.updateProfile(
                            id = userId, username = request.username,
                            email = request.email, fullName = request.fullName
                        )

                        if (updated == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("用户不存在"))
                            return@put
                        }
                        call.respond(ApiResponse.success(updated.toProfileResponse(), "更新成功"))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "更新失败"))
                    }
                }
            }
        }


        // POST /auth/change-password - 修改密码
        route("/auth") {
            authenticate {
                post("/change-password") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val request = call.receive<ChangePasswordRequest>()

                        if (!ValidationUtils.validatePassword(request.newPassword)) {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse.badRequest("密码长度至少8位"))
                            return@post
                        }

                        val user = userRepository.findById(userId)
                        if (user == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("用户不存在"))
                            return@post
                        }

                        if (!passwordHasher.verify(request.currentPassword, user.passwordHash)) {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse.badRequest("当前密码不正确"))
                            return@post
                        }

                        if (passwordHasher.verify(request.newPassword, user.passwordHash)) {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse.badRequest("新密码不能与当前密码相同"))
                            return@post
                        }

                        val newPasswordHash = passwordHasher.hash(request.newPassword)
                        val success = userRepository.updatePasswordHash(userId, newPasswordHash)

                        if (success) {
                            call.respond(ApiResponse.success(null, "密码修改成功"))
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, ApiResponse.serverError("密码修改失败"))
                        }
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "修改失败"))
                    }
                }
            }
        }
    }
}
