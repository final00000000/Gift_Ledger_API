package com.giftledger.adapters.http.routes

import com.giftledger.adapters.exceptions.*
import com.giftledger.adapters.http.ApiResponse
import com.giftledger.adapters.http.PagedData
import com.giftledger.adapters.validation.ValidationUtils
import com.giftledger.application.ports.EventBookRepository
import com.giftledger.application.ports.GiftRepository
import com.giftledger.application.ports.GuestRepository
import com.giftledger.domain.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Serializable
data class CreateGiftRequest(
    val guestId: String,
    val isReceived: Boolean,
    val amount: Double,
    val eventType: String,
    val eventBookId: String? = null,
    val occurredAt: String,
    val note: String? = null
)

@Serializable
data class UpdateGiftRequest(
    val guestId: String? = null,
    val isReceived: Boolean? = null,
    val amount: Double? = null,
    val eventType: String? = null,
    val eventBookId: String? = null,
    val occurredAt: String? = null,
    val note: String? = null
)

@Serializable
data class GiftResponse(
    val id: String,
    val guestId: String,
    val isReceived: Boolean,
    val amount: Double,
    val eventType: String,
    val eventBookId: String?,
    val occurredAt: String,
    val note: String?,
    val relatedGiftId: String?,
    val isReturned: Boolean,
    val createdAt: String,
    val updatedAt: String
)


private fun Gift.toResponse() = GiftResponse(
    id = id.value.toString(),
    guestId = guestId.value.toString(),
    isReceived = isReceived,
    amount = amount.amount.toDouble(),
    eventType = eventType,
    eventBookId = eventBookId?.value?.toString(),
    occurredAt = occurredAt.toString(),
    note = note,
    relatedGiftId = relatedGiftId?.value?.toString(),
    isReturned = isReturned,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

fun Application.giftRoutes() {
    val giftRepository: GiftRepository by inject()
    val guestRepository: GuestRepository by inject()
    val eventBookRepository: EventBookRepository by inject()

    routing {
        route("/gifts") {
            authenticate {
                // POST /gifts - 创建礼物记录
                post {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val request = call.receive<CreateGiftRequest>()

                        val validation = ValidationUtils.Validator()
                            .require("guestId", request.guestId, "宾客ID不能为空")
                            .requireValidUUID("guestId", request.guestId, "宾客ID格式无效")
                            .requirePositive("amount", request.amount, "金额必须大于0")
                            .require("eventType", request.eventType, "活动类型不能为空")
                            .require("occurredAt", request.occurredAt, "发生日期不能为空")
                            .requireValidDate("occurredAt", request.occurredAt, "日期格式无效")
                            .build()

                        if (!validation.isValid) {
                            val errorMsg = validation.errors.values.joinToString("; ")
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(errorMsg))
                            return@post
                        }

                        if (request.eventBookId != null && !ValidationUtils.validateUUID(request.eventBookId)) {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse.badRequest("活动账本ID格式无效"))
                            return@post
                        }

                        val guestId = GuestId(UUID.fromString(request.guestId))
                        val eventBookId = request.eventBookId?.let { EventBookId(UUID.fromString(it)) }

                        if (guestRepository.findById(userId, guestId) == null) {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse.badRequest("宾客不存在"))
                            return@post
                        }

                        if (eventBookId != null && eventBookRepository.findById(userId, eventBookId) == null) {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse.badRequest("活动账本不存在"))
                            return@post
                        }

                        val gift = giftRepository.create(
                            userId = userId, guestId = guestId, isReceived = request.isReceived,
                            amount = Money(BigDecimal.valueOf(request.amount)), eventType = request.eventType,
                            eventBookId = eventBookId, occurredAt = LocalDate.parse(request.occurredAt),
                            note = request.note
                        )
                        call.respond(HttpStatusCode.Created, ApiResponse.created(gift.toResponse()))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "创建失败"))
                    }
                }


                // GET /gifts - 获取礼物列表
                get {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))

                        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                        val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                        val guestIdStr = call.request.queryParameters["guestId"]
                        val eventBookIdStr = call.request.queryParameters["eventBookId"]
                        val isReceivedStr = call.request.queryParameters["isReceived"]

                        val guestId = guestIdStr?.let { if (ValidationUtils.validateUUID(it)) GuestId(UUID.fromString(it)) else null }
                        val eventBookId = eventBookIdStr?.let { if (ValidationUtils.validateUUID(it)) EventBookId(UUID.fromString(it)) else null }
                        val isReceived = isReceivedStr?.toBooleanStrictOrNull()

                        val (gifts, total) = giftRepository.listByUserIdPaginated(userId, page, pageSize, guestId, eventBookId, isReceived)

                        call.respond(ApiResponse.successPaged(
                            data = gifts.map { it.toResponse() },
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

                // GET /gifts/{id} - 获取单个礼物详情
                get("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val idStr = call.parameters["id"] ?: throw ValidationException("缺少ID参数")

                        if (!ValidationUtils.validateUUID(idStr)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("无效的ID格式"))
                            return@get
                        }

                        val giftId = GiftId(UUID.fromString(idStr))
                        val gift = giftRepository.findById(userId, giftId)

                        if (gift == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("礼物记录不存在"))
                            return@get
                        }
                        call.respond(ApiResponse.success(gift.toResponse()))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "查询失败"))
                    }
                }


                // PUT /gifts/{id} - 更新礼物记录
                put("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val idStr = call.parameters["id"] ?: throw ValidationException("缺少ID参数")

                        if (!ValidationUtils.validateUUID(idStr)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("无效的ID格式"))
                            return@put
                        }

                        val giftId = GiftId(UUID.fromString(idStr))
                        val request = call.receive<UpdateGiftRequest>()

                        if (request.amount != null && request.amount <= 0) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("金额必须大于0"))
                            return@put
                        }
                        if (request.occurredAt != null && !ValidationUtils.validateDate(request.occurredAt)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("日期格式无效"))
                            return@put
                        }
                        if (request.guestId != null && !ValidationUtils.validateUUID(request.guestId)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("宾客ID格式无效"))
                            return@put
                        }
                        if (request.eventBookId != null && !ValidationUtils.validateUUID(request.eventBookId)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("活动账本ID格式无效"))
                            return@put
                        }

                        val guestId = request.guestId?.let { GuestId(UUID.fromString(it)) }
                        val eventBookId = request.eventBookId?.let { EventBookId(UUID.fromString(it)) }

                        if (guestId != null && guestRepository.findById(userId, guestId) == null) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("宾客不存在"))
                            return@put
                        }
                        if (eventBookId != null && eventBookRepository.findById(userId, eventBookId) == null) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("活动账本不存在"))
                            return@put
                        }

                        val updated = giftRepository.update(
                            userId = userId, id = giftId, guestId = guestId, isReceived = request.isReceived,
                            amount = request.amount?.let { Money(BigDecimal.valueOf(it)) }, eventType = request.eventType,
                            eventBookId = eventBookId, occurredAt = request.occurredAt?.let { LocalDate.parse(it) },
                            note = request.note
                        )

                        if (updated == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("礼物记录不存在"))
                            return@put
                        }
                        call.respond(ApiResponse.success(updated.toResponse(), "更新成功"))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "更新失败"))
                    }
                }

                // DELETE /gifts/{id} - 删除礼物记录
                delete("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val idStr = call.parameters["id"] ?: throw ValidationException("缺少ID参数")

                        if (!ValidationUtils.validateUUID(idStr)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("无效的ID格式"))
                            return@delete
                        }

                        val giftId = GiftId(UUID.fromString(idStr))
                        val existing = giftRepository.findById(userId, giftId)
                        if (existing == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("礼物记录不存在"))
                            return@delete
                        }

                        val deleted = giftRepository.delete(userId, giftId)
                        if (deleted) {
                            call.respond(ApiResponse.success(null, "删除成功"))
                        } else {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("礼物记录不存在"))
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
