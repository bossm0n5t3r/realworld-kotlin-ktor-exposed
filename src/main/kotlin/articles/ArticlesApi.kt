package me.bossm0n5t3r.articles

import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import me.bossm0n5t3r.userIdOrNull

fun Route.articlesApi(articlesService: ArticlesService) {
    authenticate(optional = true) {
        get("/articles") {
            val userId = call.userIdOrNull()
            val articleFilterDto = ArticleFilterDto(call.parameters)
            val articles = articlesService.getAllArticles(userId, articleFilterDto)
            call.respond(articles)
        }
    }
}
