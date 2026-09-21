package me.bossm0n5t3r.tags

import java.util.UUID
import me.bossm0n5t3r.articles.Articles
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass

object Tags : UUIDTable() {
    val tagName = varchar("tag_name", 255).uniqueIndex()
}

class TagEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TagEntity>(Tags)

    var tagName by Tags.tagName
}

object ArticleTags : UUIDTable() {
    val articleId =
        reference(
            "article_id",
            Articles,
            onDelete = ReferenceOption.CASCADE,
            onUpdate = ReferenceOption.CASCADE,
        )
    val tagId =
        reference(
            "tag_id",
            Tags,
            onDelete = ReferenceOption.CASCADE,
            onUpdate = ReferenceOption.CASCADE,
        )

    init {
        uniqueIndex(articleId, tagId)
    }
}

class ArticleTagEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ArticleTagEntity>(ArticleTags)

    var articleId by ArticleTags.articleId
    var tagId by ArticleTags.tagId
}
