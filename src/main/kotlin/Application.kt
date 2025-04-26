package me.bossm0n5t3r

import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureRouting()
}
