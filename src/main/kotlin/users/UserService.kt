package me.bossm0n5t3r.users

import me.bossm0n5t3r.uitilities.PasswordEncoder

class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    suspend fun login(loginUserDto: LoginUserDto): UserDto {
        require(loginUserDto.email.isNotBlank()) { "Email must not be blank" }
        require(loginUserDto.password.isNotBlank()) { "Password must not be blank" }

        val userEntity = userRepository.findUserEntityByEmail(loginUserDto.email)
        requireNotNull(userEntity) { "Not found User" }

        require(
            passwordEncoder.matches(
                loginUserDto.password,
                userEntity.hashedPassword,
                userEntity.salt,
            ),
        ) { "Invalid password" }

        return UserDto(userEntity)
    }

    suspend fun register(createUserDto: CreateUserDto): UserDto {
        val userWithEmail = userRepository.findUserEntityByEmail(createUserDto.email)
        require(userWithEmail == null) { "User with this email already registered" }

        val userWithUsername = userRepository.findUserEntityByUsername(createUserDto.username)
        require(userWithUsername == null) { "User with this username already registered" }

        val (hashedPassword, hexEncodedSalt) = passwordEncoder.encode(createUserDto.password)

        return userRepository.createUser(
            username = createUserDto.username,
            email = createUserDto.email,
            hashedPassword = hashedPassword,
            salt = hexEncodedSalt,
        )
    }

    private suspend fun getUserEntityById(id: String): UserEntity = userRepository.getUserEntityById(id)

    suspend fun getUserById(id: String): UserDto = UserDto(getUserEntityById(id))

    suspend fun getAllUsers() = userRepository.getAllUsers()

    suspend fun updateUser(
        id: String,
        updateUserDto: UpdateUserDto,
    ): UserDto {
        val userEntity = getUserEntityById(id)

        if (updateUserDto.email != null) {
            val existingUserWithEmail = userRepository.findUserEntityByEmail(updateUserDto.email)
            require(existingUserWithEmail == null || existingUserWithEmail.id.value.toString() == id) {
                "User with this email already registered"
            }
        }

        if (updateUserDto.username != null) {
            val existingUserWithUsername = userRepository.findUserEntityByUsername(updateUserDto.username)
            require(existingUserWithUsername == null || existingUserWithUsername.id.value.toString() == id) {
                "User with this username already registered"
            }
        }

        val updatedUserName = updateUserDto.username ?: userEntity.username
        val updatedEmail = updateUserDto.email ?: userEntity.email
        val updatedHashedPassword =
            updateUserDto.password
                ?.let { passwordEncoder.hashPassword(it, userEntity.salt) }
                ?: userEntity.hashedPassword
        val updatedSalt = userEntity.salt
        val updatedBio = updateUserDto.bio ?: userEntity.bio
        val updatedImage = updateUserDto.image ?: userEntity.image

        return userRepository.updateUser(
            userEntity,
            updatedUserName,
            updatedEmail,
            updatedHashedPassword,
            updatedSalt,
            updatedBio,
            updatedImage,
        )
    }
}
