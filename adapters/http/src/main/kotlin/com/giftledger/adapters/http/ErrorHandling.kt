package com.giftledger.adapters.http

import com.giftledger.adapters.exceptions.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

fun Application.configureErrorHandling() {
    val logger = LoggerFactory.getLogger("ErrorHandling")

    install(StatusPages) {
        // 认证异常 - 401
        exception<AuthenticationException> { call, cause ->
            logger.warn("认证失败: ${cause.message}")
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse.unauthorized(cause.message ?: "未授权")
            )
        }

        // 验证异常 - 400
        exception<ValidationException> { call, cause ->
            logger.warn("验证失败: ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.badRequest(cause.message ?: "验证失败")
            )
        }

        // 资源不存在 - 404
        exception<NotFoundException> { call, cause ->
            logger.warn("资源不存在: ${cause.message}")
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse.notFound(cause.message ?: "资源不存在")
            )
        }

        // 资源冲突 - 409
        exception<ConflictException> { call, cause ->
            logger.warn("资源冲突: ${cause.message}")
            call.respond(
                HttpStatusCode.Conflict,
                ApiResponse.conflict(cause.message ?: "资源冲突")
            )
        }

        // 参数错误 - 400
        exception<IllegalArgumentException> { call, cause ->
            logger.warn("参数错误: ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.badRequest(cause.message ?: "请求参数错误")
            )
        }

        // 参数状态错误 - 400
        exception<IllegalStateException> { call, cause ->
            logger.warn("状态错误: ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.badRequest(cause.message ?: "操作状态错误")
            )
        }

        // 其他未捕获异常 - 500
        exception<Throwable> { call, cause ->
            logger.error("服务器内部错误", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.serverError("服务器内部错误")
            )
        }
    }
}