package me.bossm0n5t3r.articles

import me.bossm0n5t3r.profiles.ProfileDto

data class CommentDto(
    val id: Long,
    val createdAt: String,
    val updatedAt: String,
    val body: String,
    val author: ProfileDto,
) {
    constructor(commentEntity: CommentEntity, profileDto: ProfileDto) : this(
        id = commentEntity.id.value,
        createdAt = commentEntity.createdAt.toString(),
        updatedAt = commentEntity.updatedAt.toString(),
        body = commentEntity.body,
        author = profileDto,
    )
}

data class CreateCommentDto(
    val body: String,
)

data class CommentWrapper<T>(
    val comment: T,
)

data class CommentsWrapper<T>(
    val comments: List<T>,
)
