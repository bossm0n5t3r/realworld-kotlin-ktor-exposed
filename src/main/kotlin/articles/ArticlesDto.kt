package me.bossm0n5t3r.articles

import me.bossm0n5t3r.profiles.ProfileDto

data class ArticleWrapper<T>(
    val article: T,
)

data class ArticlesWrapper<T>(
    val articles: List<T>,
    val articlesCount: Int,
)

data class CreateArticleDto(
    val title: String,
    val description: String,
    val body: String,
    val tagList: List<String> = emptyList(),
)

data class UpdateArticleDto(
    val title: String? = null,
    val description: String? = null,
    val body: String? = null,
)

data class ArticlesDto(
    val slug: String,
    val title: String,
    val description: String,
    val body: String,
    val tagList: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val favorited: Boolean = false,
    val favoritesCount: Int = 0,
    val author: ProfileDto,
)
