package com.giftledger.adapters.exceptions

import kotlinx.serialization.Serializable

// 验证异常 - 请求数据验证失败
class ValidationException(
    message: String,
    val fieldErrors: Map<String, String>? = null
) : Exception(message)

// 资源未找到异常
class NotFoundException(message: String) : Exception(message)

// 约束冲突异常 - 如删除有关联数据的记录
class ConstraintViolationException(message: String) : Exception(message)

// 认证异常
class AuthenticationException(message: String) : Exception(message)

// 授权异常
class AuthorizationException(message: String) : Exception(message)

// 资源冲突异常 - 如唯一键冲突
class ConflictException(message: String) : Exception(message)

// 错误响应DTO
@Serializable
data class ErrorResponse(
    val error: String,
    val type: String = "Error",
    val details: Map<String, String>? = null
)
