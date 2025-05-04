package me.bossm0n5t3r.articles

import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.users.UserEntity
import org.jetbrains.exposed.sql.and

class FavoriteArticlesRepository(
    private val databaseManager: DatabaseManager,
) {
    suspend fun favoriteArticle(
        articleEntity: ArticleEntity,
        userEntity: UserEntity,
    ) = databaseManager.dbQuery {
        FavoriteArticleEntity.new {
            this.articleId = articleEntity.id
            this.userId = userEntity.id
        }
    }

    suspend fun unfavoriteArticle(
        articleEntity: ArticleEntity,
        userEntity: UserEntity,
    ) = databaseManager.dbQuery {
        FavoriteArticleEntity
            .find { (FavoriteArticles.userId eq userEntity.id) and (FavoriteArticles.articleId eq articleEntity.id) }
            .singleOrNull()
            ?.delete()
    }

    suspend fun isFavoritedArticle(
        articleEntity: ArticleEntity,
        userEntity: UserEntity,
    ) = databaseManager.dbQuery {
        FavoriteArticleEntity
            .find { (FavoriteArticles.userId eq userEntity.id) and (FavoriteArticles.articleId eq articleEntity.id) }
            .empty()
            .not()
    }

    suspend fun getFavoriteArticleIds(userEntity: UserEntity) =
        databaseManager.dbQuery {
            FavoriteArticleEntity.find { FavoriteArticles.userId eq userEntity.id }.map { it.articleId.value.toString() }
        }

    suspend fun getFavoritesCount(articleEntity: ArticleEntity) =
        databaseManager.dbQuery {
            FavoriteArticleEntity.find { FavoriteArticles.articleId eq articleEntity.id }.count()
        }
}
