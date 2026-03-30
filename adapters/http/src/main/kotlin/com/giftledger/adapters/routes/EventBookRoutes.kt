package com.giftledger.adapters.http.routes

import com.giftledger.adapters.exceptions.*
import com.giftledger.adapters.http.ApiResponse
import com.giftledger.adapters.validation.ValidationUtils
import com.giftledger.application.ports.EventBookRepository
import com.giftledger.application.ports.GiftRepository
import com.giftledger.domain.EventBook
import com.giftledger.domain.EventBookId
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
import java.time.LocalDate
import java.util.*

@Serializable
data class CreateEventBookRequest(
    val name: String,
    val type: String,
    val eventDate: String,
    val lunarDate: String? = null,
    val note: String? = null
)

@Serializable
data class UpdateEventBookRequest(
    val name: String? = null,
    val type: String? = null,
    val eventDate: String? = null,
    val lunarDate: String? = null,
    val note: String? = null
)

@Serializable
data class EventBookResponse(
    val id: String,
    val name: String,
    val type: String,
    val eventDate: String,
    val lunarDate: String?,
    val note: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class EventBookListData(
    val eventBooks: List<EventBookResponse>,
    val total: Int
)


private fun EventBook.toResponse() = EventBookResponse(
    id = id.value.toString(),
    name = name,
    type = type,
    eventDate = eventDate.toString(),
    lunarDate = lunarDate,
    note = note,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

fun Application.eventBookRoutes() {
    val eventBookRepository: EventBookRepository by inject()
    val giftRepository: GiftRepository by inject()

    routing {
        route("/event-books") {
            authenticate {
                // POST /event-books - 创建活动账本
                post {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val request = call.receive<CreateEventBookRequest>()

                        val validation = ValidationUtils.Validator()
                            .require("name", request.name, "名称不能为空")
                            .require("type", request.type, "类型不能为空")
                            .require("eventDate", request.eventDate, "活动日期不能为空")
                            .requireValidDate("eventDate", request.eventDate, "日期格式无效")
                            .build()

                        if (!validation.isValid) {
                            val errorMsg = validation.errors.values.joinToString("; ")
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(errorMsg))
                            return@post
                        }

                        val eventBook = eventBookRepository.create(
                            userId = userId, name = request.name, type = request.type,
                            eventDate = LocalDate.parse(request.eventDate),
                            lunarDate = request.lunarDate, note = request.note
                        )
                        call.respond(HttpStatusCode.Created, ApiResponse.created(eventBook.toResponse()))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "创建失败"))
                    }
                }

                // GET /event-books - 获取活动账本列表
                get {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val eventBooks = eventBookRepository.listByUserId(userId)
                        call.respond(ApiResponse.success(EventBookListData(eventBooks.map { it.toResponse() }, eventBooks.size)))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "查询失败"))
                    }
                }


                // GET /event-books/{id} - 获取单个活动账本详情
                get("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val idStr = call.parameters["id"] ?: throw ValidationException("缺少ID参数")

                        if (!ValidationUtils.validateUUID(idStr)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("无效的ID格式"))
                            return@get
                        }

                        val eventBookId = EventBookId(UUID.fromString(idStr))
                        val eventBook = eventBookRepository.findById(userId, eventBookId)

                        if (eventBook == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("活动账本不存在"))
                            return@get
                        }
                        call.respond(ApiResponse.success(eventBook.toResponse()))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "查询失败"))
                    }
                }

                // PUT /event-books/{id} - 更新活动账本
                put("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val idStr = call.parameters["id"] ?: throw ValidationException("缺少ID参数")

                        if (!ValidationUtils.validateUUID(idStr)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("无效的ID格式"))
                            return@put
                        }

                        val eventBookId = EventBookId(UUID.fromString(idStr))
                        val request = call.receive<UpdateEventBookRequest>()

                        if (request.eventDate != null && !ValidationUtils.validateDate(request.eventDate)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("日期格式无效"))
                            return@put
                        }

                        val existing = eventBookRepository.findById(userId, eventBookId)
                        if (existing == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("活动账本不存在"))
                            return@put
                        }

                        val updated = eventBookRepository.update(
                            userId = userId, id = eventBookId,
                            name = request.name ?: existing.name, type = request.type ?: existing.type,
                            eventDate = request.eventDate?.let { LocalDate.parse(it) } ?: existing.eventDate,
                            lunarDate = request.lunarDate ?: existing.lunarDate, note = request.note ?: existing.note
                        )

                        if (!updated) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("活动账本不存在"))
                            return@put
                        }

                        val updatedEventBook = eventBookRepository.findById(userId, eventBookId)!!
                        call.respond(ApiResponse.success(updatedEventBook.toResponse(), "更新成功"))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "更新失败"))
                    }
                }


                // DELETE /event-books/{id} - 删除活动账本
                delete("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val idStr = call.parameters["id"] ?: throw ValidationException("缺少ID参数")

                        if (!ValidationUtils.validateUUID(idStr)) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest("无效的ID格式"))
                            return@delete
                        }

                        val eventBookId = EventBookId(UUID.fromString(idStr))
                        val existing = eventBookRepository.findById(userId, eventBookId)
                        if (existing == null) {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("活动账本不存在"))
                            return@delete
                        }

                        val giftCount = giftRepository.countByEventBookId(userId, eventBookId)
                        if (giftCount > 0) {
                            call.respond(HttpStatusCode.BadRequest, 
                                ApiResponse.badRequest("无法删除：该活动账本有 $giftCount 条关联的礼物记录"))
                            return@delete
                        }

                        val deleted = eventBookRepository.delete(userId, eventBookId)
                        if (deleted) {
                            call.respond(ApiResponse.success(null, "删除成功"))
                        } else {
                            call.respond(HttpStatusCode.NotFound, ApiResponse.notFound("活动账本不存在"))
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
