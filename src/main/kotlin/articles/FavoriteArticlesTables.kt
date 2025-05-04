package me.bossm0n5t3r.articles

import me.bossm0n5t3r.users.Users
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object FavoriteArticles : UUIDTable() {
    val userId = reference("user_id", Users)
    val articleId = reference("article_id", Articles)

    init {
        uniqueIndex(userId, articleId)
    }
}

class FavoriteArticleEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<FavoriteArticleEntity>(FavoriteArticles)

    var userId by FavoriteArticles.userId
    var articleId by FavoriteArticles.articleId
}
