package me.bossm0n5t3r.users

import me.bossm0n5t3r.configurations.DatabaseManager
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UsersRepository(
    private val databaseManager: DatabaseManager,
) {
    suspend fun createUser(
        username: String,
        email: String,
        hashedPassword: String,
        salt: String,
    ) = databaseManager.dbQuery {
        UserEntity
            .new {
                this.username = username
                this.email = email
                this.hashedPassword = hashedPassword
                this.salt = salt
            }.let { UserDto(it) }
    }

    suspend fun findUserEntityByEmail(email: String) =
        databaseManager.dbQuery {
            UserEntity.find { Users.email eq email }.firstOrNull()
        }

    suspend fun findUserEntityByUsername(username: String) =
        databaseManager.dbQuery {
            UserEntity.find { Users.username eq username }.firstOrNull()
        }

    suspend fun getUserEntityById(id: String): UserEntity {
        val uuid = UUID.fromString(id)
        return databaseManager.dbQuery { requireNotNull(UserEntity.findById(uuid)) { "Not found user by id $id" } }
    }

    suspend fun getUserEntityById(id: EntityID<UUID>) = databaseManager.dbQuery { UserEntity[id] }

    suspend fun getAllUsers() = databaseManager.dbQuery { UserEntity.all().map { UserDto(it) } }

    suspend fun updateUser(
        userEntity: UserEntity,
        updatedUserNames: String,
        updatedEmails: String,
        updatedHashedPasswords: String,
        updatedSalts: String,
        updatedBio: String,
        updatedImages: String? = null,
    ): UserDto {
        databaseManager.dbQuery {
            userEntity.username = updatedUserNames
            userEntity.email = updatedEmails
            userEntity.hashedPassword = updatedHashedPasswords
            userEntity.salt = updatedSalts
            userEntity.bio = updatedBio
            updatedImages?.let { userEntity.image = it }
            userEntity.flush()
        }
        return UserDto(userEntity)
    }
}
