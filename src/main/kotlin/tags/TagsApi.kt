package me.bossm0n5t3r.tags

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.tagsApi(tagsService: TagsService) {
    get("/tags") {
        call.respond(tagsService.getAllTags())
    }
}
