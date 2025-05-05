package me.bossm0n5t3r.articles

import me.bossm0n5t3r.profiles.FollowingsRepository
import me.bossm0n5t3r.profiles.ProfileDto
import me.bossm0n5t3r.tags.TagsRepository
import me.bossm0n5t3r.users.UserEntity
import me.bossm0n5t3r.users.UsersRepository

class ArticlesService(
    private val usersRepository: UsersRepository,
    private val followingsRepository: FollowingsRepository,
    private val articlesRepository: ArticlesRepository,
    private val favoriteArticlesRepository: FavoriteArticlesRepository,
    private val tagsRepository: TagsRepository,
    private val commentsRepository: CommentsRepository,
) {
    suspend fun getAllArticles(
        userId: String?,
        articleFilterDto: ArticleFilterDto,
    ): ArticlesWrapper<ArticleDto> {
        val userEntity = userId?.let { usersRepository.getUserEntityById(it) }
        val author = articleFilterDto.author?.let { usersRepository.findUserEntityByUsername(it) }
        val favoritedByUser = articleFilterDto.favorited?.let { usersRepository.findUserEntityByUsername(it) }
        val tag = articleFilterDto.tag?.let { tagsRepository.getTagOrNull(it) }

        val articles =
            articlesRepository
                .getAllArticles(author, articleFilterDto.limit, articleFilterDto.offset)
                .filter { articleEntity ->
                    favoritedByUser == null || favoriteArticlesRepository.isFavoritedArticle(articleEntity, favoritedByUser)
                }.filter {
                    tag == null || tagsRepository.isArticleHasTag(it, tag)
                }.map {
                    getArticleDto(it, userEntity)
                }

        return ArticlesWrapper(articles, articles.count())
    }

    private suspend fun getArticleDto(
        articleEntity: ArticleEntity,
        currentUser: UserEntity? = null,
    ): ArticleDto {
        val author = usersRepository.getUserEntityById(articleEntity.authorId)
        val articleTags = tagsRepository.getAllTagsByArticle(articleEntity).map { it.tagName }
        val favoritesCount = favoriteArticlesRepository.getFavoritesCount(articleEntity)
        val isFavorited = currentUser?.let { favoriteArticlesRepository.isFavoritedArticle(articleEntity, it) } ?: false
        val following = currentUser?.let { followingsRepository.isFollowing(author.id.value.toString(), it.id.value.toString()) } ?: false
        val authorProfile = ProfileDto(author, following)

        return ArticleDto(
            slug = articleEntity.slug,
            title = articleEntity.title,
            description = articleEntity.description,
            body = articleEntity.body,
            tagList = articleTags,
            createdAt = articleEntity.createdAt.toString(),
            updatedAt = articleEntity.updatedAt.toString(),
            favorited = isFavorited,
            favoritesCount = favoritesCount,
            author = authorProfile,
        )
    }

    suspend fun getFeedArticles(
        userId: String,
        limit: Int,
        offset: Int,
    ): ArticlesWrapper<ArticleDto> {
        val userEntity = usersRepository.getUserEntityById(userId)
        val followingUsers =
            followingsRepository
                .getAllFollowingsByUserId(userEntity.id.value.toString())
                .map { usersRepository.getUserEntityById(it.userId.toString()) }
        val articles =
            articlesRepository
                .getAllArticles(followingUsers, limit, offset)
                .map { getArticleDto(it, userEntity) }
        return ArticlesWrapper(articles, articles.count())
    }

    suspend fun getArticleBySlug(slug: String): ArticleWrapper<ArticleDto> {
        val article = articlesRepository.getArticleBySlug(slug) ?: error("Article not found")
        return ArticleWrapper(getArticleDto(article))
    }

    suspend fun createArticle(
        userId: String,
        createArticleDto: CreateArticleDto,
    ): ArticleWrapper<ArticleDto> {
        val userEntity = usersRepository.getUserEntityById(userId)
        val articleEntity = articlesRepository.createArticle(userEntity, createArticleDto)
        for (tagName in createArticleDto.tagList) {
            val tag = tagsRepository.getOrCreateTag(tagName)
            tagsRepository.createArticleTagEntity(articleEntity, tag)
        }
        return ArticleWrapper(getArticleDto(articleEntity, userEntity))
    }

    suspend fun updateArticle(
        userId: String,
        slug: String,
        updateArticleDto: UpdateArticleDto,
    ): ArticleWrapper<ArticleDto> {
        val userEntity = usersRepository.getUserEntityById(userId)
        val articleEntity = articlesRepository.getArticleBySlug(slug) ?: error("Article not found")
        if (articleEntity.authorId != userEntity.id) error("Cannot update article: not the author")
        val updatedArticle = articlesRepository.updateArticle(articleEntity, updateArticleDto)
        return ArticleWrapper(getArticleDto(updatedArticle, userEntity))
    }

    suspend fun deleteArticle(
        userId: String,
        slug: String,
    ) {
        val userEntity = usersRepository.getUserEntityById(userId)
        val articleEntity = articlesRepository.getArticleBySlug(slug) ?: error("Article not found")
        if (articleEntity.authorId != userEntity.id) error("Cannot delete article: not the author")
        articlesRepository.deleteArticle(articleEntity)
    }

    suspend fun favoriteArticle(
        userId: String,
        slug: String,
    ): ArticleWrapper<ArticleDto> {
        val userEntity = usersRepository.getUserEntityById(userId)
        val articleEntity = articlesRepository.getArticleBySlug(slug) ?: error("Article not found")
        favoriteArticlesRepository.favoriteArticle(articleEntity, userEntity)
        return ArticleWrapper(getArticleDto(articleEntity, userEntity))
    }

    suspend fun unfavoriteArticle(
        userId: String,
        slug: String,
    ): ArticleWrapper<ArticleDto> {
        val userEntity = usersRepository.getUserEntityById(userId)
        val articleEntity = articlesRepository.getArticleBySlug(slug) ?: error("Article not found")
        favoriteArticlesRepository.unfavoriteArticle(articleEntity, userEntity)
        return ArticleWrapper(getArticleDto(articleEntity, userEntity))
    }

    suspend fun addComment(
        userId: String,
        slug: String,
        createCommentDto: CreateCommentDto,
    ): CommentWrapper<CommentDto> {
        val userEntity = usersRepository.getUserEntityById(userId)
        val articleEntity = articlesRepository.getArticleBySlug(slug) ?: error("Article not found")
        val commentEntity = commentsRepository.createComment(userEntity, articleEntity, createCommentDto.body)
        return CommentWrapper(getCommentDto(commentEntity, userEntity))
    }

    private suspend fun getCommentDto(
        commentEntity: CommentEntity,
        currentUser: UserEntity? = null,
    ): CommentDto {
        val commentAuthor = usersRepository.getUserEntityById(commentEntity.userId)
        val following =
            currentUser?.let { followingsRepository.isFollowing(commentAuthor.id.value.toString(), it.id.value.toString()) } ?: false
        val commentAuthorProfile = ProfileDto(commentAuthor, following)
        return CommentDto(commentEntity, commentAuthorProfile)
    }

    suspend fun getCommentsForArticle(
        userId: String? = null,
        slug: String,
    ): CommentsWrapper<CommentDto> {
        val userEntity = userId?.let { usersRepository.getUserEntityById(it) }
        val articleEntity = articlesRepository.getArticleBySlug(slug) ?: error("Article not found")
        val comments =
            commentsRepository
                .getCommentsForArticle(articleEntity)
                .map { getCommentDto(it, userEntity) }
        return CommentsWrapper(comments)
    }

    suspend fun deleteComment(
        userId: String,
        slug: String,
        commentId: Long,
    ) {
        val userEntity = usersRepository.getUserEntityById(userId)
        val articleEntity = articlesRepository.getArticleBySlug(slug) ?: error("Article not found")
        val commentEntity =
            commentsRepository
                .getCommentsForArticle(articleEntity)
                .find { it.id.value == commentId }
                ?: error("Comment not found")
        if (commentEntity.userId != userEntity.id) error("Cannot delete comment: not the author")
        commentsRepository.deleteComment(commentEntity)
    }
}
