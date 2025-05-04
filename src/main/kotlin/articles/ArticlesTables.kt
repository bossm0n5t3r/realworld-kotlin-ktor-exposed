package me.bossm0n5t3r.articles

import me.bossm0n5t3r.users.Users
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.UUID

object Articles : UUIDTable() {
    val slug = varchar("slug", 255).uniqueIndex()
    var title = varchar("title", 255)
    val description = varchar("description", 255)
    val body = varchar("body", 255)
    val authorId = reference("user_id", Users)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}

class ArticleEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ArticleEntity>(Articles)

    var slug by Articles.slug
    var title by Articles.title
    var description by Articles.description
    var body by Articles.body
    var authorId by Articles.authorId
    var createdAt by Articles.createdAt
    var updatedAt by Articles.updatedAt
}
