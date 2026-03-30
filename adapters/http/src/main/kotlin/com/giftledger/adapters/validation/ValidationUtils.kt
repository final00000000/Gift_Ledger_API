package com.giftledger.adapters.validation

import java.time.LocalDate
import java.time.format.DateTimeParseException

object ValidationUtils {
    // 金额验证：必须大于0
    fun validateAmount(amount: Double): Boolean = amount > 0

    // 日期验证：ISO格式 yyyy-MM-dd
    fun parseDate(dateStr: String): LocalDate? = try {
        LocalDate.parse(dateStr)
    } catch (e: DateTimeParseException) {
        null
    }

    fun validateDate(dateStr: String): Boolean = parseDate(dateStr) != null

    // 邮箱验证
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    fun validateEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

    // 密码验证：至少8个字符
    fun validatePassword(password: String): Boolean = password.length >= 8

    // 非空字符串验证
    fun validateNotBlank(value: String?): Boolean = !value.isNullOrBlank()

    // UUID验证
    fun validateUUID(value: String): Boolean = try {
        java.util.UUID.fromString(value)
        true
    } catch (e: IllegalArgumentException) {
        false
    }

    // 手机号验证（简单验证，支持国际格式）
    private val PHONE_REGEX = Regex("^[+]?[0-9]{7,15}$")
    fun validatePhone(phone: String?): Boolean = phone == null || phone.isBlank() || PHONE_REGEX.matches(phone)

    // 验证结果类
    data class ValidationResult(
        val isValid: Boolean,
        val errors: Map<String, String> = emptyMap()
    ) {
        companion object {
            fun success() = ValidationResult(true)
            fun failure(errors: Map<String, String>) = ValidationResult(false, errors)
            fun failure(field: String, message: String) = ValidationResult(false, mapOf(field to message))
        }
    }

    // 验证构建器
    class Validator {
        private val errors = mutableMapOf<String, String>()

        fun require(field: String, value: String?, message: String = "$field 不能为空"): Validator {
            if (value.isNullOrBlank()) errors[field] = message
            return this
        }

        fun requirePositive(field: String, value: Double?, message: String = "$field 必须大于0"): Validator {
            if (value == null || value <= 0) errors[field] = message
            return this
        }

        fun requireValidDate(field: String, value: String?, message: String = "$field 日期格式无效"): Validator {
            if (value != null && !validateDate(value)) errors[field] = message
            return this
        }

        fun requireValidEmail(field: String, value: String?, message: String = "$field 邮箱格式无效"): Validator {
            if (value != null && !validateEmail(value)) errors[field] = message
            return this
        }

        fun requireValidPassword(field: String, value: String?, message: String = "$field 密码长度至少8位"): Validator {
            if (value != null && !validatePassword(value)) errors[field] = message
            return this
        }

        fun requireValidUUID(field: String, value: String?, message: String = "$field UUID格式无效"): Validator {
            if (value != null && !validateUUID(value)) errors[field] = message
            return this
        }

        fun build(): ValidationResult {
            return if (errors.isEmpty()) ValidationResult.success()
            else ValidationResult.failure(errors)
        }
    }
}
