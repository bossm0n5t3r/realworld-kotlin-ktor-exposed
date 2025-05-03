package me.bossm0n5t3r.profiles

import me.bossm0n5t3r.users.Users
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object Followings : UUIDTable() {
    val userId = uuid("user_id").references(Users.id)
    val followerId = uuid("follower_id").references(Users.id)

    init {
        uniqueIndex(userId, followerId)
    }
}

class FollowingEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<FollowingEntity>(Followings)

    var userId by Followings.userId
    var followerId by Followings.followerId
}
