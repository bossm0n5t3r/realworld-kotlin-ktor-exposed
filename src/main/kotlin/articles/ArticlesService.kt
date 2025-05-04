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
        currentUser: UserEntity?,
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
}
