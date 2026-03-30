package com.giftledger.adapters.http

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.AuthScheme
import io.github.smiley4.ktorswaggerui.data.AuthType
import io.ktor.http.*
import io.ktor.server.response.*
import com.giftledger.infrastructure.jwt.JwtConfig

fun main() {
    val serverPort = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = serverPort, host = "0.0.0.0") {
        configureModules()
        configureRoutes()
    }.start(wait = true)
}

private fun parseCorsOrigin(origin: String): Pair<List<String>, String> {
    val trimmed = origin.trim().trimEnd('/')
    val parts = trimmed.split("://", limit = 2)
    return if (parts.size == 2) {
        val scheme = parts[0].trim().lowercase().takeIf { it.isNotEmpty() } ?: "http"
        val hostPort = parts[1].trim()
        listOf(scheme) to hostPort
    } else {
        listOf("http", "https") to trimmed
    }
}

fun Application.configureModules() {
    configureDI()
    configureErrorHandling()
    
    // 安装限流插件
    install(RateLimitPlugin)

    install(SwaggerUI) {
        swagger {
            swaggerUrl = "/swagger-ui"
            forwardRoot = true
        }
        info {
            title = "Gift Ledger API"
            version = "1.0.0"
            description = "礼物账本 - 完整的API文档"
        }
        securityScheme("bearer") {
            type = AuthType.HTTP
            scheme = AuthScheme.BEARER
            bearerFormat = "JWT"
            description = "使用JWT令牌进行身份验证"
        }
    }

    install(CORS) {
        val env = System.getenv()
        val allowedOrigins = env["CORS_ALLOWED_ORIGINS"]
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val allowCredentialsFromEnv = env["CORS_ALLOW_CREDENTIALS"]
            ?.trim()
            ?.lowercase()
            ?.let { it == "1" || it == "true" || it == "yes" || it == "y" || it == "on" }
            ?: false

        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowMethod(io.ktor.http.HttpMethod.Patch)
        allowHeader("Authorization")
        allowHeader("Content-Type")

        if (allowedOrigins.isEmpty()) {
            allowCredentials = false
            anyHost()
        } else {
            allowCredentials = allowCredentialsFromEnv
            allowedOrigins.forEach { origin ->
                val (schemes, hostPort) = parseCorsOrigin(origin)
                allowHost(hostPort, schemes = schemes)
            }
        }
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }

    install(Authentication) {
        val jwtConfig = JwtConfig.fromEnv()
        val algorithm = Algorithm.HMAC256(jwtConfig.secret)
        jwt {
            realm = jwtConfig.realm
            verifier {
                JWT.require(algorithm)
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            }
            validate { credential ->
                val tokenType = credential.payload.getClaim("type")?.asString()
                if (credential.payload.audience.contains(jwtConfig.audience) && tokenType == "access") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ApiResponse.unauthorized("令牌无效或已过期"))
            }
        }
    }
}
