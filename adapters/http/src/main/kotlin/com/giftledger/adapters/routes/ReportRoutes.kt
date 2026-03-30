package com.giftledger.adapters.http.routes

import com.giftledger.adapters.exceptions.AuthenticationException
import com.giftledger.adapters.http.ApiResponse
import com.giftledger.application.services.ReportService
import com.giftledger.domain.UserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.time.LocalDate
import java.util.*

@Serializable
data class ReportSummary(
    val totalReceived: Double,
    val totalSent: Double,
    val netAmount: Double,
    val giftCount: Int,
    val guestCount: Int,
    val period: String
)

fun Application.reportRoutes() {
    val reportService: ReportService by inject()

    routing {
        route("/reports") {
            authenticate {
                // GET /reports/summary - 获取统计报告
                get("/summary") {
                    try {
                        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException("未授权")
                        val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))

                        val fromStr = call.request.queryParameters["from"]
                        val toStr = call.request.queryParameters["to"]

                        val from = fromStr?.let { LocalDate.parse(it) } ?: LocalDate.now().minusYears(1)
                        val to = toStr?.let { LocalDate.parse(it) } ?: LocalDate.now()

                        val summary = reportService.getSummary(userId, from, to)

                        val response = ReportSummary(
                            totalReceived = summary.totalReceived,
                            totalSent = summary.totalSent,
                            netAmount = summary.netBalance,
                            giftCount = summary.pendingCount,
                            guestCount = 0,
                            period = "$from 至 $to"
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