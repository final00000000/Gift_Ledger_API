package com.giftledger.adapters.http.routes

import com.giftledger.adapters.exceptions.AuthenticationException
import com.giftledger.adapters.http.ApiResponse
import com.giftledger.domain.UserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant
import java.util.*

fun Application.exportRoutes() {
    routing {
        route("/exports") {
            authenticate {
                // GET /exports/json - 导出JSON数据
                get("/json") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))

                        // TODO: 实现导出功能
                        val exportData = mapOf(
                            "exportedAt" to Instant.now().toString(),
                            "guests" to emptyList<Map<String, Any>>(),
                            "eventBooks" to emptyList<Map<String, Any>>(),
                            "gifts" to emptyList<Map<String, Any>>()
                        )

                        call.respond(ApiResponse.success(exportData))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "导出失败"))
                    }
                }
            }
        }
    }
}