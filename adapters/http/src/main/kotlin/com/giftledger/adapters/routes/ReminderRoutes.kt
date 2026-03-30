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
import java.util.*

fun Application.reminderRoutes() {
    routing {
        route("/reminders") {
            authenticate {
                // GET /reminders/pending - 获取待还礼提醒
                get("/pending") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))

                        // TODO: 实现提醒功能
                        val response = mapOf(
                            "reminders" to emptyList<String>(),
                            "total" to 0
                        )

                        call.respond(ApiResponse.success(response))
                    } catch (e: AuthenticationException) {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized(e.message ?: "未授权"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.badRequest(e.message ?: "查询失败"))
                    }
                }
            }
        }
    }
}