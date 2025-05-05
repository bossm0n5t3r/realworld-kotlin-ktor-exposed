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

        get("/articles/{slug}/comments") {
            val userId = call.userIdOrNull()
            val slug = call.parameters["slug"] ?: error("Invalid request")
            call.respond(articlesService.getCommentsForArticle(userId, slug))
        }
    }

    get("/articles/{slug}") {
        val slug = call.parameters["slug"] ?: error("Invalid request")
        call.respond(articlesService.getArticleBySlug(slug))
    }

    authenticate {
        post("/articles") {
            val userId = call.userId()
            val createArticleDto = call.receive<ArticleWrapper<CreateArticleDto>>()
            call.respond(articlesService.createArticle(userId, createArticleDto.article))
        }

        get("/articles/feed") {
            val userId = call.userId()
            val limit = call.parameters["limit"]?.toIntOrNull() ?: 20
            val offset = call.parameters["offset"]?.toIntOrNull() ?: 0
            call.respond(articlesService.getFeedArticles(userId, limit, offset))
        }

        put("/articles/{slug}") {
            val slug = call.parameters["slug"] ?: error("Invalid request")
            val userId = call.userId()
            val updateArticleDto = call.receive<ArticleWrapper<UpdateArticleDto>>()
            call.respond(articlesService.updateArticle(userId, slug, updateArticleDto.article))
        }

        delete("/articles/{slug}") {
            val slug = call.parameters["slug"] ?: error("Invalid request")
            val userId = call.userId()
            call.respond(articlesService.deleteArticle(userId, slug))
        }

        post("/articles/{slug}/favorite") {
            val slug = call.parameters["slug"] ?: error("Invalid request")
            val userId = call.userId()
            call.respond(articlesService.favoriteArticle(userId, slug))
        }

        delete("/articles/{slug}/favorite") {
            val slug = call.parameters["slug"] ?: error("Invalid request")
            val userId = call.userId()
            call.respond(articlesService.unfavoriteArticle(userId, slug))
        }

        post("/articles/{slug}/comments") {
            val slug = call.parameters["slug"] ?: error("Invalid request")
            val userId = call.userId()
            val createCommentDto = call.receive<CommentWrapper<CreateCommentDto>>()
            call.respond(articlesService.addComment(userId, slug, createCommentDto.comment))
        }

        delete("/articles/{slug}/comments/{id}") {
            val slug = call.parameters["slug"] ?: error("Invalid request")
            val userId = call.userId()
            val commentId = call.parameters["id"]?.toLongOrNull() ?: error("Invalid request")
            call.respond(articlesService.deleteComment(userId, slug, commentId))
        }
    }
}
