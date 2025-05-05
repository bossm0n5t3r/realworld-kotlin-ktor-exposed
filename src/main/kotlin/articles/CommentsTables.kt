package me.bossm0n5t3r.articles

import me.bossm0n5t3r.users.Users
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Comments : LongIdTable() {
    val createdAt = timestamp("createdAt").default(Instant.now())
    val updatedAt = timestamp("updatedAt").default(Instant.now())
    val body = text("body")
    val userId = reference("user_id", Users, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
    val articleId = reference("article_id", Articles, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
}

class CommentEntity(
    id: EntityID<Long>,
) : LongEntity(id) {
    companion object : LongEntityClass<CommentEntity>(Comments)

    var createdAt by Comments.createdAt
    var updatedAt by Comments.updatedAt
    var body by Comments.body
    var userId by Comments.userId
    var articleId by Comments.articleId
}
