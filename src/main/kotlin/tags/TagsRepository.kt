package me.bossm0n5t3r.tags

import me.bossm0n5t3r.articles.ArticleEntity
import me.bossm0n5t3r.configurations.DatabaseManager
import org.jetbrains.exposed.sql.and

class TagsRepository(
    private val databaseManager: DatabaseManager,
) {
    suspend fun getOrCreateTag(tagName: String) =
        databaseManager.dbQuery {
            TagEntity.find { Tags.tagName eq tagName }.firstOrNull() ?: TagEntity.new { this.tagName = tagName }
        }

    suspend fun getTagOrNull(tagName: String) = databaseManager.dbQuery { TagEntity.find { Tags.tagName eq tagName }.firstOrNull() }

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

    suspend fun getAllTagsByArticle(articleEntity: ArticleEntity) =
        databaseManager.dbQuery {
            ArticleTagEntity.find { ArticleTags.articleId eq articleEntity.id }.map { TagEntity[it.tagId] }
        }

    suspend fun isArticleHasTag(
        articleEntity: ArticleEntity,
        tagEntity: TagEntity,
    ) = databaseManager.dbQuery {
        ArticleTagEntity
            .find { (ArticleTags.articleId eq articleEntity.id) and (ArticleTags.tagId eq tagEntity.id) }
            .empty()
            .not()
    }
}
