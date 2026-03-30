package com.giftledger.adapters.http

import com.giftledger.adapters.persistence.exposed.*
import com.giftledger.application.services.*
import com.giftledger.application.services.ReminderService
import com.giftledger.application.ports.*
import com.giftledger.infrastructure.security.PasswordHasherImpl
import com.giftledger.infrastructure.jwt.JwtService
import com.giftledger.infrastructure.db.DatabaseFactory
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDI() {
    // 初始化数据库连接
    val dataSource = DatabaseFactory.init()
    Database.connect(dataSource)

    install(Koin) {
        modules(module {
            // Security
            single<PasswordHasher> {
                val impl = PasswordHasherImpl()
                object : PasswordHasher {
                    override fun hash(password: String): String = impl.hash(password)
                    override fun verify(password: String, hash: String): Boolean = impl.verify(password, hash)
                }
            }

            // JWT
            single<JwtTokenProvider> {
                val service = JwtService()
                object : JwtTokenProvider {
                    override fun generateAccessToken(userId: String): String = service.generateAccessToken(userId)
                    override fun generateRefreshToken(userId: String): String = service.generateRefreshToken(userId)
                }
            }

            // MySQL Repositories (企业级持久化)
            single<UserRepository> { UserRepositoryExposed() }
            single<RefreshTokenRepository> { RefreshTokenRepositoryExposed() }
            single<GuestRepository> { GuestRepositoryExposed() }
            single<EventBookRepository> { EventBookRepositoryExposed() }
            single<GiftRepository> { GiftRepositoryExposed() }
            single<ReminderSettingsRepository> { ReminderSettingsRepositoryExposed() }
            single<ReportRepository> { ReportRepositoryExposed() }

            // Services
            single<AuthService> {
                AuthService(
                    userRepository = get(),
                    refreshTokenRepository = get(),
                    jwtProvider = get(),
                    passwordHasher = get()
                )
            }
            single<GuestService> { GuestService(get()) }
            single<EventBookService> { EventBookService(get()) }
            single<GiftService> { GiftService(get()) }
            single<ReportService> { ReportService(get()) }
            single { ReminderService(get<GiftRepository>(), get<ReminderSettingsRepository>()) }
        })
    }
}
