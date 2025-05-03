package me.bossm0n5t3r.profiles

import me.bossm0n5t3r.users.UserEntity

data class ProfileDto(
    val username: String,
    val bio: String,
    val image: String?,
    val following: Boolean,
) {
    constructor(userEntity: UserEntity) : this(
        username = userEntity.username,
        bio = userEntity.bio,
        image = userEntity.image,
        following = false,
    )

    constructor(userEntity: UserEntity, isFollowing: Boolean) : this(
        username = userEntity.username,
        bio = userEntity.bio,
        image = userEntity.image,
        following = isFollowing,
    )
}

data class ProfileResponse(
    val profile: ProfileDto? = null,
)
