package me.bossm0n5t3r.profiles

import me.bossm0n5t3r.users.UsersRepository

class ProfilesService(
    private val usersRepository: UsersRepository,
    private val followingsRepository: FollowingsRepository,
) {
    suspend fun getProfile(
        username: String,
        currentUserId: String?,
    ): ProfileResponse {
        val toUser = usersRepository.findUserEntityByUsername(username) ?: return ProfileResponse()
        val fromUser =
            currentUserId?.let { usersRepository.getUserEntityById(it) }
                ?: return ProfileResponse(ProfileDto(toUser))
        val isFollowing = followingsRepository.isFollowing(toUser.id.value.toString(), fromUser.id.value.toString())
        return ProfileResponse(ProfileDto(toUser, isFollowing))
    }

    suspend fun follow(
        username: String,
        currentUserId: String,
    ): ProfileResponse {
        val toUser = usersRepository.findUserEntityByUsername(username) ?: error("User not found")
        val fromUser = usersRepository.getUserEntityById(currentUserId)
        followingsRepository.addFollowing(toUser.id.value.toString(), fromUser.id.value.toString())
        return ProfileResponse(ProfileDto(toUser, true))
    }

    suspend fun unfollow(
        username: String,
        currentUserId: String,
    ): ProfileResponse {
        val toUser = usersRepository.findUserEntityByUsername(username) ?: error("User not found")
        val fromUser = usersRepository.getUserEntityById(currentUserId)
        followingsRepository.deleteFollowing(toUser.id.value.toString(), fromUser.id.value.toString())
        return ProfileResponse(ProfileDto(toUser, false))
    }
}
