package me.bossm0n5t3r

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import me.bossm0n5t3r.configurations.DatabaseManager
import org.koin.ktor.ext.inject

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureCORS()
    configureMonitoring()
    configureKoin()

    val databaseManager: DatabaseManager by inject()
    databaseManager.createTables()

    configureSerialization()
    configureSecurity()
    configureRouting()
}
