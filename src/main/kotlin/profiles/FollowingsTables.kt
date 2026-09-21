package me.bossm0n5t3r.profiles

import java.util.UUID
import me.bossm0n5t3r.users.Users
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass

object Followings : UUIDTable() {
    val userId = javaUUID("user_id").references(Users.id)
    val followerId = javaUUID("follower_id").references(Users.id)

    init {
        uniqueIndex(userId, followerId)
    }
}

class FollowingEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<FollowingEntity>(Followings)

    var userId by Followings.userId
    var followerId by Followings.followerId
}
