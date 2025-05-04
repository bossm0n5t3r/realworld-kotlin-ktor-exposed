package me.bossm0n5t3r.tags

import me.bossm0n5t3r.articles.ArticleEntity
import me.bossm0n5t3r.configurations.DatabaseManager

class TagsRepository(
    private val databaseManager: DatabaseManager,
) {
    suspend fun getOrCreateTag(tagName: String) =
        databaseManager.dbQuery {
            TagEntity.find { Tags.tagName eq tagName }.firstOrNull() ?: TagEntity.new { this.tagName = tagName }
        }

    suspend fun createArticleTagEntity(
        articleEntity: ArticleEntity,
        tagEntity: TagEntity,
    ) = databaseManager.dbQuery {
        ArticleTagEntity.new {
            this.articleId = articleEntity.id
            this.tagId = tagEntity.id
        }
    }

    suspend fun getAllTags() = databaseManager.dbQuery { TagEntity.all().map { it.tagName } }
}
