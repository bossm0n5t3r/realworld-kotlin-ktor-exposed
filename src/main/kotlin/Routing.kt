package me.bossm0n5t3r

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import me.bossm0n5t3r.profiles.ProfilesService
import me.bossm0n5t3r.profiles.profilesApi
import me.bossm0n5t3r.tags.TagsService
import me.bossm0n5t3r.tags.tagsApi
import me.bossm0n5t3r.users.UsersService
import me.bossm0n5t3r.users.usersApi
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    val usersService: UsersService by inject()
    val profilesService: ProfilesService by inject()
    val tagsService: TagsService by inject()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/api") {
            get {
                call.respondText("Hello World from API!")
            }

            usersApi(usersService)
            profilesApi(profilesService)
            tagsApi(tagsService)
        }
    }
}
