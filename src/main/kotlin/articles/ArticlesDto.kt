package me.bossm0n5t3r.articles

import io.ktor.http.Parameters
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

data class ArticleFilterDto(
    val tag: String? = null,
    val author: String? = null,
    val favorited: String? = null,
    val limit: Int = 20,
    val offset: Int = 0,
) {
    constructor(params: Parameters) : this(
        tag = params["tag"],
        author = params["author"],
        favorited = params["favorited"],
        limit = params["limit"]?.toIntOrNull() ?: 20,
        offset = params["offset"]?.toIntOrNull() ?: 0,
    )
}

data class ArticleDto(
    val slug: String,
    val title: String,
    val description: String,
    val body: String,
    val tagList: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val favorited: Boolean = false,
    val favoritesCount: Long = 0,
    val author: ProfileDto,
)
