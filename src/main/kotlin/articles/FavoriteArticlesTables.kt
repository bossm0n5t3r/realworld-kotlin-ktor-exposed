package me.bossm0n5t3r.articles

import java.util.UUID
import me.bossm0n5t3r.users.Users
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass

object FavoriteArticles : UUIDTable() {
    val userId = reference("user_id", Users)
    val articleId = reference("article_id", Articles)

    init {
        uniqueIndex(userId, articleId)
    }
}

class FavoriteArticleEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<FavoriteArticleEntity>(FavoriteArticles)

    var userId by FavoriteArticles.userId
    var articleId by FavoriteArticles.articleId
}
