package me.bossm0n5t3r.articles

import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import me.bossm0n5t3r.userId
import me.bossm0n5t3r.userIdOrNull

fun Route.articlesApi(articlesService: ArticlesService) {
    authenticate(optional = true) {
        get("/articles") {
            val userId = call.userIdOrNull()
            val articleFilterDto = ArticleFilterDto(call.parameters)
            call.respond(articlesService.getAllArticles(userId, articleFilterDto))
        }
    }

    get("/article/{slug}") {
        val slug = call.parameters["slug"] ?: error("Invalid request")
        call.respond(articlesService.getArticleBySlug(slug))
    }

    authenticate {
        post("/articles") {
            val userId = call.userId()
            val createArticleDto = call.receive<ArticleWrapper<CreateArticleDto>>()
            call.respond(articlesService.createArticle(userId, createArticleDto.article))
        }

        put("/articles/{slug}") {
            val slug = call.parameters["slug"] ?: error("Invalid request")
            val userId = call.userId()
            val updateArticleDto = call.receive<ArticleWrapper<UpdateArticleDto>>()
            call.respond(articlesService.updateArticle(slug, userId, updateArticleDto.article))
        }

        delete("/articles/{slug}") {
            val slug = call.parameters["slug"] ?: error("Invalid request")
            val userId = call.userId()
            call.respond(articlesService.deleteArticle(slug, userId))
        }

        post("/articles/{slug}/favorite") {
            val slug = call.parameters["slug"] ?: error("Invalid request")
            val userId = call.userId()
            call.respond(articlesService.favoriteArticle(slug, userId))
        }

        delete("/articles/{slug}/favorite") {
            val slug = call.parameters["slug"] ?: error("Invalid request")
            val userId = call.userId()
            call.respond(articlesService.unfavoriteArticle(slug, userId))
        }
    }
}
