package me.bossm0n5t3r

import io.ktor.server.application.Application
import io.ktor.server.application.install
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
import me.bossm0n5t3r.uitilities.PasswordEncoder
import me.bossm0n5t3r.uitilities.PasswordEncoderImpl
import me.bossm0n5t3r.users.UsersRepository
import me.bossm0n5t3r.users.UserService
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single<DatabaseManager> { DatabaseManagerImpl() }
            },
            module {
                single { UsersRepository(get()) }
                single<PasswordEncoder> { PasswordEncoderImpl() }
            },
            module {
                single { UserService(get(), get()) }
            },
        )
    }
}
