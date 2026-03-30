package com.giftledger.infrastructure.db

import com.giftledger.infrastructure.config.Env

data class DbConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maximumPoolSize: Int,
    val minimumIdle: Int,
    val connectionTimeoutMs: Long,
    val idleTimeoutMs: Long,
    val maxLifetimeMs: Long,
    val leakDetectionThresholdMs: Long,
    val flywayBaselineOnMigrate: Boolean,
) {
    init {
        require(maximumPoolSize >= 1) { "maximumPoolSize must be >= 1" }
        require(minimumIdle in 0..maximumPoolSize) { "minimumIdle must be between 0 and maximumPoolSize" }
        require(connectionTimeoutMs >= 250) { "connectionTimeoutMs must be >= 250" }
    }

    companion object {
        fun fromEnv(env: Map<String, String> = System.getenv()): DbConfig {
            val jdbcUrl = Env.getNonBlank(env, "DB_JDBC_URL") ?: buildJdbcUrl(
                host = Env.string(env, "DB_HOST", "localhost"),
                port = Env.int(env, "DB_PORT", 3306),
                database = Env.string(env, "DB_NAME", "gift_ledger"),
                useSSL = Env.boolean(env, "DB_USE_SSL", false),
            )

            return DbConfig(
                jdbcUrl = jdbcUrl,
                username = Env.string(env, "DB_USER", "root"),
                password = Env.string(env, "DB_PASSWORD", ""),
                maximumPoolSize = Env.int(env, "DB_POOL_MAX_SIZE", 10),
                minimumIdle = Env.int(env, "DB_POOL_MIN_IDLE", 2),
                connectionTimeoutMs = Env.long(env, "DB_POOL_CONNECTION_TIMEOUT_MS", 30_000L),
                idleTimeoutMs = Env.long(env, "DB_POOL_IDLE_TIMEOUT_MS", 600_000L),
                maxLifetimeMs = Env.long(env, "DB_POOL_MAX_LIFETIME_MS", 1_800_000L),
                leakDetectionThresholdMs = Env.long(env, "DB_POOL_LEAK_DETECTION_THRESHOLD_MS", 0L),
                flywayBaselineOnMigrate = Env.boolean(env, "FLYWAY_BASELINE_ON_MIGRATE", true),
            )
        }

        private fun buildJdbcUrl(host: String, port: Int, database: String, useSSL: Boolean): String {
            val sslParam = if (useSSL) "useSSL=true&requireSSL=true" else "useSSL=false"
            return "jdbc:mysql://$host:$port/$database?$sslParam&serverTimezone=UTC&characterEncoding=utf8"
        }
    }
}
