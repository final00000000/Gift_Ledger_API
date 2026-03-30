package com.giftledger.infrastructure.config

internal object Env {
    fun getNonBlank(env: Map<String, String>, key: String): String? =
        env[key]?.trim()?.takeIf { it.isNotEmpty() }

    fun string(env: Map<String, String>, key: String, default: String): String =
        getNonBlank(env, key) ?: default

    fun int(env: Map<String, String>, key: String, default: Int): Int =
        getNonBlank(env, key)?.toIntOrNull() ?: default

    fun long(env: Map<String, String>, key: String, default: Long): Long =
        getNonBlank(env, key)?.toLongOrNull() ?: default

    fun boolean(env: Map<String, String>, key: String, default: Boolean): Boolean =
        getNonBlank(env, key)?.let {
            when (it.trim().lowercase()) {
                "1", "true", "yes", "y", "on" -> true
                "0", "false", "no", "n", "off" -> false
                else -> default
            }
        } ?: default
}
