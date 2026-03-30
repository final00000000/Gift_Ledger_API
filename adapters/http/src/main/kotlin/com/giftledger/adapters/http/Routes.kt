package com.giftledger.adapters.http

import com.giftledger.adapters.http.routes.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes() {
    routing {
        // 静态文件 - API 文档首页
        staticResources("/", "static")

        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
    authRoutes()
    userRoutes()
    giftRoutes()
    eventBookRoutes()
    guestRoutes()
    reportRoutes()
    exportRoutes()
    reminderRoutes()
}