package me.bossm0n5t3r.users

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.util.UUID

object Users : UUIDTable() {
    val username = varchar("username", 255).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val hashedPassword = varchar("hashed_password", 255)
    val salt = varchar("salt", 32)
    val bio = text("bio").default("")
    val image = varchar("image", 1024).nullable()
}

class UserEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(Users)

    var username by Users.username
    var email by Users.email
    var hashedPassword by Users.hashedPassword
    var salt by Users.salt
    var bio by Users.bio
    var image by Users.image
}
