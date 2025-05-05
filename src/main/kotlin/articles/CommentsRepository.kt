package me.bossm0n5t3r.articles

import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.users.UserEntity
import java.time.Instant

class CommentsRepository(
    private val databaseManager: DatabaseManager,
) {
    suspend fun createComment(
        userEntity: UserEntity,
        articleEntity: ArticleEntity,
        body: String,
    ) = databaseManager.dbQuery {
        CommentEntity.new {
            this.createdAt = Instant.now()
            this.updatedAt = Instant.now()
            this.body = body
            this.userId = userEntity.id
            this.articleId = articleEntity.id
        }
    }

    suspend fun getCommentsForArticle(articleEntity: ArticleEntity) =
        databaseManager.dbQuery {
            CommentEntity.find { Comments.articleId eq articleEntity.id }.toList()
        }

    suspend fun deleteComment(commentEntity: CommentEntity) =
        databaseManager.dbQuery {
            commentEntity.delete()
        }
}
