package me.bossm0n5t3r.users

import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.uitilities.PasswordEncoder
import java.util.UUID

class UserService(
    private val databaseManager: DatabaseManager,
    private val passwordEncoder: PasswordEncoder,
) {
    suspend fun login(loginUserDto: LoginUserDto): UserDto {
        require(loginUserDto.email.isNotBlank()) { "Email must not be blank" }
        require(loginUserDto.password.isNotBlank()) { "Password must not be blank" }

        val user = databaseManager.dbQuery { UserEntity.find { Users.email eq loginUserDto.email }.firstOrNull() }
        requireNotNull(user) { "Not found User" }

        require(passwordEncoder.matches(loginUserDto.password, user.hashedPassword, user.salt)) { "Invalid password" }

        return UserDto(user)
    }

    suspend fun register(createUserDto: CreateUserDto): UserDto =
        databaseManager.dbQuery {
            require(UserEntity.find { Users.email eq createUserDto.email }.empty()) {
                "User already registered"
            }
            require(UserEntity.find { Users.username eq createUserDto.username }.empty()) {
                "User already registered"
            }

            val (hashedPassword, hexEncodedSalt) = passwordEncoder.encode(createUserDto.password)

            UserEntity
                .new {
                    this.username = createUserDto.username
                    this.email = createUserDto.email
                    this.hashedPassword = hashedPassword
                    this.salt = hexEncodedSalt
                }.let { UserDto(it) }
        }

    private suspend fun getUserEntityById(id: String): UserEntity {
        val uuid = UUID.fromString(id)
        return databaseManager.dbQuery { requireNotNull(UserEntity.findById(uuid)) { "Not found user by id $id" } }
    }

    suspend fun getUserById(id: String): UserDto = UserDto(getUserEntityById(id))

    suspend fun getAllUsers() = databaseManager.dbQuery { UserEntity.all().map { UserDto(it) } }

    suspend fun updateUser(
        id: String,
        updateUserDto: UpdateUserDto,
    ): UserDto {
        val userEntity = getUserEntityById(id)
        if (updateUserDto.username != null) {
            require(UserEntity.find { Users.username eq updateUserDto.username }.empty()) {
                "User already registered"
            }

            userEntity.username = updateUserDto.username
        }
        if (updateUserDto.email != null) {
            require(UserEntity.find { Users.email eq updateUserDto.email }.empty()) {
                "User already registered"
            }
            userEntity.email = updateUserDto.email
        }

        if (updateUserDto.password != null) {
            val updatedHashedPassword = passwordEncoder.hashPassword(updateUserDto.password, userEntity.salt)

            userEntity.hashedPassword = updatedHashedPassword
        }

        if (updateUserDto.bio != null) {
            userEntity.bio = updateUserDto.bio
        }

        if (updateUserDto.image != null) {
            userEntity.image = updateUserDto.image
        }

        userEntity.flush()

        return UserDto(userEntity)
    }
}
