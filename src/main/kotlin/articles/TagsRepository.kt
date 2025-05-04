package me.bossm0n5t3r.articles

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
}
