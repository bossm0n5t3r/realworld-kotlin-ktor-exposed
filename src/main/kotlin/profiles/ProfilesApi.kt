package me.bossm0n5t3r.profiles

import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import me.bossm0n5t3r.userId
import me.bossm0n5t3r.userIdOrNull

fun Route.profilesApi(profilesService: ProfilesService) {
    authenticate(optional = true) {
        get("/profiles/{username}") {
            val username = call.parameters["username"] ?: error("Invalid request")
            val currentUserId = call.userIdOrNull()
            call.respond(profilesService.getProfile(username, currentUserId))
        }
    }

    authenticate {
        post("/profiles/{username}/follow") {
            val username = call.parameters["username"] ?: error("Invalid request")
            val currentUserId = call.userId()
            call.respond(profilesService.follow(username, currentUserId))
        }

        delete("/profiles/{username}/follow") {
            val username = call.parameters["username"] ?: error("Invalid request")
            val currentUserId = call.userId()
            call.respond(profilesService.unfollow(username, currentUserId))
        }
    }
}
