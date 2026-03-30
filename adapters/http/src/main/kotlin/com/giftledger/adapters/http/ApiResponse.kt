package com.giftledger.adapters.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 统一API响应格式
 *
 * 成功响应示例:
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": { ... }
 * }
 *
 * 分页响应示例:
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": [...],
 *   "page": 1,
 *   "size": 20,
 *   "total": 100
 * }
 *
 * 错误响应示例:
 * {
 *   "code": 400,
 *   "message": "姓名不能为空",
 *   "data": null
 * }
 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
    val page: Int? = null,
    val size: Int? = null,
    val total: Int? = null
) {
    companion object {
        // 成功响应
        fun <T> success(data: T, message: String = "success"): ApiResponse<T> {
            return ApiResponse(code = 200, message = message, data = data)
        }

        // 分页成功响应
        fun <T> successPaged(
            data: List<T>,
            page: Int,
            size: Int,
            total: Int,
            message: String = "success"
        ): ApiResponse<List<T>> {
            return ApiResponse(
                code = 200,
                message = message,
                data = data,
                page = page,
                size = size,
                total = total
            )
        }

        // 创建成功
        fun <T> created(data: T, message: String = "创建成功"): ApiResponse<T> {
            return ApiResponse(code = 201, message = message, data = data)
        }

        // 删除成功（无数据返回）
        fun deleted(message: String = "删除成功"): ApiResponse<Unit> {
            return ApiResponse(code = 204, message = message, data = null)
        }

        // 错误响应
        fun error(code: Int, message: String): ApiResponse<Nothing> {
            return ApiResponse(code = code, message = message, data = null)
        }

        // 400 - 请求参数错误
        fun badRequest(message: String): ApiResponse<Nothing> {
            return error(400, message)
        }

        // 401 - 未授权
        fun unauthorized(message: String = "未授权"): ApiResponse<Nothing> {
            return error(401, message)
        }

        // 403 - 禁止访问
        fun forbidden(message: String = "禁止访问"): ApiResponse<Nothing> {
            return error(403, message)
        }

        // 404 - 资源不存在
        fun notFound(message: String = "资源不存在"): ApiResponse<Nothing> {
            return error(404, message)
        }

        // 409 - 资源冲突
        fun conflict(message: String): ApiResponse<Nothing> {
            return error(409, message)
        }

        // 500 - 服务器内部错误
        fun serverError(message: String = "服务器内部错误"): ApiResponse<Nothing> {
            return error(500, message)
        }
    }
}

// 分页响应包装
@Serializable
data class PagedData<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
) {
    companion object {
        fun <T> of(items: List<T>, total: Int, page: Int, pageSize: Int): PagedData<T> {
            val totalPages = if (pageSize > 0) (total + pageSize - 1) / pageSize else 0
            return PagedData(items, total, page, pageSize, totalPages)
        }
    }
}
