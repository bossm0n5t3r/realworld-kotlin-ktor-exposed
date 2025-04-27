package me.bossm0n5t3r

import io.ktor.server.application.Application
import io.ktor.server.application.install
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
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
        )
    }
}
