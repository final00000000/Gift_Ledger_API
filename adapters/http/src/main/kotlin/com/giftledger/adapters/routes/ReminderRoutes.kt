package com.giftledger.adapters.http.routes

import com.giftledger.adapters.exceptions.AuthenticationException
import com.giftledger.adapters.http.ApiResponse
import com.giftledger.application.services.ReminderService
import com.giftledger.domain.UserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Application.reminderRoutes() {
    val reminderService: ReminderService by inject()

    routing {
        route("/reminders") {
            authenticate {
                get("/pending") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
                        val type = call.request.queryParameters["type"] ?: "unreturned"

                        val reminders = reminderService.getPendingReminders(userId, type)
                        val response = mapOf(
                            "reminders" to reminders,
                            "total" to reminders.size
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
