package com.giftledger.adapters.http.routes

import com.giftledger.adapters.exceptions.*
import com.giftledger.adapters.http.ApiResponse
import com.giftledger.adapters.http.PagedData
import com.giftledger.adapters.validation.ValidationUtils
import com.giftledger.application.ports.GiftRepository
import com.giftledger.application.ports.GuestRepository
import com.giftledger.domain.Guest
import com.giftledger.domain.GuestId
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
data class CreateGuestRequest(
    val name: String,
    val relationship: String,
    val phone: String? = null,
    val note: String? = null
)

@Serializable
data class UpdateGuestRequest(
    val name: String? = null,
    val relationship: String? = null,
    val phone: String? = null,
    val note: String? = null
)

@Serializable
data class GuestResponse(
    val id: String,
    val name: String,
    val relationship: String,
    val phone: String?,
    val note: String?,
    val createdAt: String,
    val updatedAt: String
)

private fun Guest.toResponse() = GuestResponse(
    id = id.value.toString(),
    name = name,
    relationship = relationship,
    phone = phone,
    note = note,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)


fun Application.guestRoutes() {
    val guestRepository: GuestRepository by inject()
    val giftRepository: GiftRepository by inject()

    routing {
        route("/guests") {
            authenticate {
                // POST /guests - 创建宾客
                post {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val request = call.receive<CreateGuestRequest>()

                        val validation = ValidationUtils.Validator()
                            .require("name", request.name, "姓名不能为空")
                            .require("relationship", request.relationship, "关系不能为空")
                            .build()

                        if (!validation.isValid) {
                            val errorMsg = validation.errors.values.joinToString("; ")
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse.badRequest(errorMsg))
                            return@post
                        }

                        val guest = guestRepository.create(
                            userId = userId,
                            name = request.name,
                            relationship = request.relationship,
                            phone = request.phone,
                            note = request.note
                        )

                        call.respond(HttpStatusCode.Created, 
                            ApiResponse.created(guest.toResponse()))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "创建失败"))
                    }
                }

                // GET /guests - 获取宾客列表
                get {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))

                        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                        val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                        val search = call.request.queryParameters["search"]

                        val (guests, total) = guestRepository.listByUserIdPaginated(userId, page, pageSize, search)

                        call.respond(ApiResponse.successPaged(
                            data = guests.map { it.toResponse() },
                            page = page,
                            size = pageSize,
                            total = total
                        ))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "查询失败"))
                    }
                }


                // GET /guests/{id} - 获取单个宾客详情
                get("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val idStr = call.parameters["id"] ?: throw ValidationException("缺少ID参数")

                        if (!ValidationUtils.validateUUID(idStr)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("无效的ID格式"))
                            return@get
                        }

                        val guestId = GuestId(UUID.fromString(idStr))
                        val guest = guestRepository.findById(userId, guestId)

                        if (guest == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("宾客不存在"))
                            return@get
                        }

                        call.respond(ApiResponse.success(guest.toResponse()))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "查询失败"))
                    }
                }

                // PUT /guests/{id} - 更新宾客
                put("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val idStr = call.parameters["id"] ?: throw ValidationException("缺少ID参数")

                        if (!ValidationUtils.validateUUID(idStr)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("无效的ID格式"))
                            return@put
                        }

                        val guestId = GuestId(UUID.fromString(idStr))
                        val request = call.receive<UpdateGuestRequest>()

                        if (request.name != null && request.name.isBlank()) {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse.badRequest("姓名不能为空"))
                            return@put
                        }

                        val updated = guestRepository.update(
                            userId = userId,
                            id = guestId,
                            name = request.name,
                            relationship = request.relationship,
                            phone = request.phone,
                            note = request.note
                        )

                        if (updated == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("宾客不存在"))
                            return@put
                        }

                        call.respond(ApiResponse.success(updated.toResponse(), "更新成功"))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "更新失败"))
                    }
                }


                // DELETE /guests/{id} - 删除宾客
                delete("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val idStr = call.parameters["id"] ?: throw ValidationException("缺少ID参数")

                        if (!ValidationUtils.validateUUID(idStr)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("无效的ID格式"))
                            return@delete
                        }

                        val guestId = GuestId(UUID.fromString(idStr))

                        val existing = guestRepository.findById(userId, guestId)
                        if (existing == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("宾客不存在"))
                            return@delete
                        }

                        val giftCount = giftRepository.countByGuestId(userId, guestId)
                        if (giftCount > 0) {
                            call.respond(HttpStatusCode.BadRequest, 
                                ApiResponse.badRequest("无法删除：该宾客有 $giftCount 条关联的礼物记录"))
                            return@delete
                        }

                        val deleted = guestRepository.delete(userId, guestId)
                        if (deleted) {
                            call.respond(ApiResponse.success(null, "删除成功"))
                        } else {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("宾客不存在"))
                        }
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "删除失败"))
                    }
                }
            }
        }
    }
}
