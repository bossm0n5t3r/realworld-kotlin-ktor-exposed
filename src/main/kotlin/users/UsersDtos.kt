package me.bossm0n5t3r.users

import me.bossm0n5t3r.securities.JwtProvider

data class UserDto(
    val email: String,
    val token: String,
    val username: String,
    val bio: String,
    val image: String? = null,
) {
    constructor(userEntity: UserEntity) : this(
        email = userEntity.email,
        token = JwtProvider.createJWT(userEntity.id.value.toString()),
        username = userEntity.username,
        bio = userEntity.bio,
        image = userEntity.image,
    )
}

data class CreateUserDto(
    val username: String,
    val email: String,
    val password: String,
)

data class LoginUserDto(
    val email: String,
    val password: String,
)

data class UpdateUserDto(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val bio: String? = null,
    val image: String? = null,
)

data class UserWrapper<T>(
    val user: T,
)
