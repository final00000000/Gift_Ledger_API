package com.giftledger.infrastructure.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

object DatabaseFactory {
    @Volatile
    private var hikariDataSource: HikariDataSource? = null

    val dataSource: DataSource
        get() = hikariDataSource ?: error("DatabaseFactory.init() must be called before using dataSource")

    fun init(config: DbConfig = DbConfig.fromEnv()): DataSource {
        synchronized(this) {
            hikariDataSource?.let { return it }

            val hikariConfig = HikariConfig().apply {
                jdbcUrl = config.jdbcUrl
                username = config.username
                password = config.password

                maximumPoolSize = config.maximumPoolSize
                minimumIdle = config.minimumIdle
                connectionTimeout = config.connectionTimeoutMs
                idleTimeout = config.idleTimeoutMs
                maxLifetime = config.maxLifetimeMs

                if (config.leakDetectionThresholdMs > 0) {
                    leakDetectionThreshold = config.leakDetectionThresholdMs
                }

                poolName = "gift-ledger-db"
            }

            val ds = HikariDataSource(hikariConfig)
            try {
                migrate(ds, config)
            } catch (ex: Exception) {
                ds.close()
                throw ex
            }

            hikariDataSource = ds
            return ds
        }
    }

    fun close() {
        synchronized(this) {
            hikariDataSource?.close()
            hikariDataSource = null
        }
    }

    private fun migrate(dataSource: DataSource, config: DbConfig) {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(config.flywayBaselineOnMigrate)
            .load()

        flyway.migrate()
    }
}
