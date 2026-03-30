package com.giftledger.adapters.http.routes

import com.giftledger.adapters.exceptions.ValidationException
import com.giftledger.adapters.http.ApiResponse
import com.giftledger.adapters.http.ok
import com.giftledger.application.errors.ValidationException
import com.giftledger.application.services.ExportService
import com.giftledger.application.services.ReportService
import com.giftledger.application.services.ReminderService
import com.giftledger.domain.UserId
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.get
import java.time.LocalDate
import java.util.UUID

@Serializable
data class ReminderSettingsRequest(
    val dueDays: Int,
    val warnDays: Int,
    val maxRemindCount: Int,
    val template: String,
)

@Serializable
data class ReminderSettingsResponse(
    val dueDays: Int,
    val warnDays: Int,
    val maxRemindCount: Int,
    val template: String,
)

fun Route.reportRoutes() {
    val reportService = get<ReportService>()

    route("/reports") {
        get("/summary") {
            val principal = call.principal<JWTPrincipal>() ?: throw ValidationException("Missing principal")
            val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
            val from = call.request.queryParameters["from"]?.let { LocalDate.parse(it) }
            val to = call.request.queryParameters["to"]?.let { LocalDate.parse(it) }
            val summary = reportService.getSummary(userId, from, to)
            call.respond(ok(summary))
        }

        get("/guests") {
            val principal = call.principal<JWTPrincipal>() ?: throw ValidationException("Missing principal")
            val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
            val from = call.request.queryParameters["from"]?.let { LocalDate.parse(it) }
            val to = call.request.queryParameters["to"]?.let { LocalDate.parse(it) }
            val totals = reportService.getGuestTotals(userId, from, to)
            call.respond(ok(totals))
        }

        get("/event-books") {
            val principal = call.principal<JWTPrincipal>() ?: throw ValidationException("Missing principal")
            val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
            val from = call.request.queryParameters["from"]?.let { LocalDate.parse(it) }
            val to = call.request.queryParameters["to"]?.let { LocalDate.parse(it) }
            val totals = reportService.getEventBookTotals(userId, from, to)
            call.respond(ok(totals))
        }
    }
}

fun Route.reminderRoutes() {
    val reminderService = get<ReminderService>()

    route("/reminders") {
        get("/pending") {
            val principal = call.principal<JWTPrincipal>() ?: throw ValidationException("Missing principal")
            val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
            val type = call.request.queryParameters["type"] ?: "unreturned"
            val reminders = reminderService.getPendingReminders(userId, type)
            call.respond(ok(reminders))
        }

        post("/auto-link") {
            val principal = call.principal<JWTPrincipal>() ?: throw ValidationException("Missing principal")
            val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
            val (linked, skipped) = reminderService.autoLinkGifts(userId)
            call.respond(ok(mapOf("linked" to linked, "skipped" to skipped)))
        }

        get("/settings") {
            val principal = call.principal<JWTPrincipal>() ?: throw ValidationException("Missing principal")
            val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
            val settings = reminderService.getReminderSettings(userId)
            call.respond(ok(ReminderSettingsResponse(settings.dueDays, settings.warnDays, settings.maxRemindCount, settings.template)))
        }

        post("/settings") {
            val principal = call.principal<JWTPrincipal>() ?: throw ValidationException("Missing principal")
            val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
            val req = call.receive<ReminderSettingsRequest>()
            val settings = reminderService.updateReminderSettings(userId, req.dueDays, req.warnDays, req.maxRemindCount, req.template)
            call.respond(ok(ReminderSettingsResponse(settings.dueDays, settings.warnDays, settings.maxRemindCount, settings.template)))
        }
    }
}

fun Route.exportRoutes() {
    val exportService = get<ExportService>()

    route("/exports") {
        get("/json") {
            val principal = call.principal<JWTPrincipal>() ?: throw ValidationException("Missing principal")
            val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
            val json = exportService.exportJson(userId)
            call.response.header(HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "export.json").toString())
            call.respondBytes(json.toByteArray(Charsets.UTF_8), ContentType.Application.Json)
        }

        get("/excel") {
            val principal = call.principal<JWTPrincipal>() ?: throw ValidationException("Missing principal")
            val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
            val bytes = exportService.exportExcelBytes(userId)
            call.response.header(HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "export.csv").toString())
            call.respondBytes(bytes, ContentType.Text.CSV)
        }

        get("/pending/excel") {
            val principal = call.principal<JWTPrincipal>() ?: throw ValidationException("Missing principal")
            val userId = UserId(UUID.fromString(principal.payload.getClaim("uid").asString()))
            val bytes = exportService.exportPendingExcelBytes(userId)
            call.response.header(HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "pending.csv").toString())
            call.respondBytes(bytes, ContentType.Text.CSV)
        }
    }
}
