package me.bossm0n5t3r.profiles

import me.bossm0n5t3r.configurations.DatabaseManager
import org.jetbrains.exposed.sql.and
import java.util.UUID

class FollowingsRepository(
    private val databaseManager: DatabaseManager,
) {
    suspend fun getAllFollowingsByUserId(userId: String) =
        databaseManager.dbQuery {
            FollowingEntity.find { Followings.followerId eq UUID.fromString(userId) }.toList()
        }

    suspend fun isFollowing(
        userId: String,
        followerId: String,
    ) = databaseManager.dbQuery {
        FollowingEntity
            .find {
                (Followings.userId eq UUID.fromString(userId)) and (Followings.followerId eq UUID.fromString(followerId))
            }.empty()
            .not()
    }

    suspend fun addFollowing(
        userId: String,
        followerId: String,
    ) = databaseManager.dbQuery {
        FollowingEntity.new {
            this.userId = UUID.fromString(userId)
            this.followerId = UUID.fromString(followerId)
        }
    }

    suspend fun deleteFollowing(
        userId: String,
        followerId: String,
    ) = databaseManager.dbQuery {
        FollowingEntity
            .find {
                (Followings.userId eq UUID.fromString(userId)) and (Followings.followerId eq UUID.fromString(followerId))
            }.singleOrNull()
            ?.delete()
    }
}
