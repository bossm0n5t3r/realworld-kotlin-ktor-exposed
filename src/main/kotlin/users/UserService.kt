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
        require(userWithEmail == null) { "User already registered" }

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

        if (updateUserDto.username != null) {
            userEntity.username = updateUserDto.username
        }

        if (updateUserDto.email != null) {
            val existingUser = userRepository.findUserEntityByEmail(updateUserDto.email)
            require(existingUser == null || existingUser.id.value.toString() == id) {
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

        return userRepository.updateUser(userEntity)
    }
}
