package me.bossm0n5t3r.articles

import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.uitilities.StringUtil.toSlug
import me.bossm0n5t3r.users.UserEntity
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder

class ArticlesRepository(
    private val databaseManager: DatabaseManager,
) {
    suspend fun getAllArticles(
        author: UserEntity? = null,
        limit: Int,
        offset: Int,
    ) = databaseManager.dbQuery {
        ArticleEntity
            .find { if (author != null) Articles.authorId eq author.id else Op.TRUE }
            .limit(limit)
            .offset(offset.toLong())
            .orderBy(Articles.createdAt to SortOrder.DESC)
            .toList()
    }

    suspend fun createArticle(
        author: UserEntity,
        createArticleDto: CreateArticleDto,
    ) = databaseManager.dbQuery {
        ArticleEntity.new {
            this.title = createArticleDto.title
            this.slug = createArticleDto.title.toSlug()
            this.description = createArticleDto.description
            this.body = createArticleDto.body
            this.authorId = author.id
        }
    }

    suspend fun updateArticle(
        articleEntity: ArticleEntity,
        updateArticleDto: UpdateArticleDto,
    ) = databaseManager.dbQuery {
        articleEntity.title = updateArticleDto.title ?: articleEntity.title
        articleEntity.slug = updateArticleDto.title?.toSlug() ?: articleEntity.slug
        articleEntity.description = updateArticleDto.description ?: articleEntity.description
        articleEntity.body = updateArticleDto.body ?: articleEntity.body
        articleEntity.flush()
        articleEntity
    }

    suspend fun getArticleBySlug(slug: String) =
        databaseManager.dbQuery {
            ArticleEntity.find { Articles.slug eq slug }.singleOrNull()
        }
}
