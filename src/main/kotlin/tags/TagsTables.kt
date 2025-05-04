package me.bossm0n5t3r.tags

import me.bossm0n5t3r.articles.Articles
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.UUID

object Tags : UUIDTable() {
    val tagName = varchar("tag_name", 255).uniqueIndex()
}

class TagEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
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

class ArticleTagEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ArticleTagEntity>(ArticleTags)

    var articleId by ArticleTags.articleId
    var tagId by ArticleTags.tagId
}
