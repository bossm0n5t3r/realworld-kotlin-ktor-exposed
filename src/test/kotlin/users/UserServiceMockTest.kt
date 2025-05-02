package me.bossm0n5t3r.users

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.uitilities.PasswordEncoder
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserServiceMockTest {
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val passwordEncoder: PasswordEncoder = mockk(relaxed = true)
    private val userService: UserService = UserService(userRepository, passwordEncoder)

    @Test
    fun testRegister() =
        runBlocking {
            // Given
            val createUserDto =
                CreateUserDto(
                    username = "testuser",
                    email = "test@example.com",
                    password = "password123",
                )

            val hashedPassword = "hashedPassword"
            val salt = "salt"
            val userId = UUID.randomUUID()
            val userEntity = mockk<UserEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(userId, Users)
            every { userEntity.username } returns createUserDto.username
            every { userEntity.email } returns createUserDto.email
            every { userEntity.hashedPassword } returns hashedPassword
            every { userEntity.salt } returns salt
            every { userEntity.bio } returns ""
            every { userEntity.image } returns null

            // Create a UserDto from the entity
            val userDto = UserDto(userEntity)

            // Mock the password encoder
            every { passwordEncoder.encode(createUserDto.password) } returns Pair(hashedPassword, salt)

            // Mock the UserRepository methods
            coEvery { userRepository.findUserEntityByEmail(createUserDto.email) } returns null
            coEvery {
                userRepository.createUser(
                    username = createUserDto.username,
                    email = createUserDto.email,
                    hashedPassword = hashedPassword,
                    salt = salt,
                )
            } returns userDto

            // When
            val result = userService.register(createUserDto)

            // Then
            assertEquals(createUserDto.username, result.username)
            assertEquals(createUserDto.email, result.email)
            assertEquals("", result.bio)
            assertEquals(null, result.image)

            // Verify that the repository methods were called
            coVerify { userRepository.findUserEntityByEmail(createUserDto.email) }
            coVerify {
                userRepository.createUser(
                    username = createUserDto.username,
                    email = createUserDto.email,
                    hashedPassword = hashedPassword,
                    salt = salt,
                )
            }
            verify { passwordEncoder.encode(createUserDto.password) }
        }

    @Test
    fun testLogin() =
        runBlocking {
            // Given
            val loginUserDto =
                LoginUserDto(
                    email = "test@example.com",
                    password = "password123",
                )

            val hashedPassword = "hashedPassword"
            val salt = "salt"
            val userEntity = mockk<UserEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.randomUUID(), Users)
            every { userEntity.username } returns "testuser"
            every { userEntity.email } returns loginUserDto.email
            every { userEntity.hashedPassword } returns hashedPassword
            every { userEntity.salt } returns salt
            every { userEntity.bio } returns ""
            every { userEntity.image } returns null

            // Create a UserDto from the entity
            val userDto = UserDto(userEntity)

            // Mock the UserRepository methods
            coEvery { userRepository.findUserEntityByEmail(loginUserDto.email) } returns userEntity

            // Mock the password encoder
            every { passwordEncoder.matches(loginUserDto.password, hashedPassword, salt) } returns true

            // When
            val result = userService.login(loginUserDto)

            // Then
            assertEquals("testuser", result.username)
            assertEquals(loginUserDto.email, result.email)

            // Verify that the password encoder was called
            verify { passwordEncoder.matches(loginUserDto.password, hashedPassword, salt) }

            // Verify that the repository methods were called
            coVerify { userRepository.findUserEntityByEmail(loginUserDto.email) }
        }

    @Test
    fun testLoginWithInvalidPassword() =
        runBlocking {
            // Given
            val loginUserDto =
                LoginUserDto(
                    email = "test@example.com",
                    password = "wrongpassword",
                )

            val hashedPassword = "hashedPassword"
            val salt = "salt"
            val userEntity = mockk<UserEntity>()

            // Set up the user entity properties
            every { userEntity.username } returns "testuser"
            every { userEntity.email } returns loginUserDto.email
            every { userEntity.hashedPassword } returns hashedPassword
            every { userEntity.salt } returns salt

            // Mock the UserRepository methods
            coEvery { userRepository.findUserEntityByEmail(loginUserDto.email) } returns userEntity

            // Mock the password encoder to return false for invalid password
            every { passwordEncoder.matches(loginUserDto.password, hashedPassword, salt) } returns false

            // When/Then
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    userService.login(loginUserDto)
                }

            assertEquals("Invalid password", exception.message)

            // Verify that the password encoder was called
            verify { passwordEncoder.matches(loginUserDto.password, hashedPassword, salt) }

            // Verify that the repository methods were called
            coVerify { userRepository.findUserEntityByEmail(loginUserDto.email) }
        }

    @Test
    fun testGetUserById() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID()
            val userEntity = mockk<UserEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(userId, Users)
            every { userEntity.username } returns "testuser"
            every { userEntity.email } returns "test@example.com"
            every { userEntity.bio } returns ""
            every { userEntity.image } returns null

            // Mock the UserRepository methods
            coEvery { userRepository.getUserEntityById(userId.toString()) } returns userEntity

            // When
            val result = userService.getUserById(userId.toString())

            // Then
            assertEquals("testuser", result.username)
            assertEquals("test@example.com", result.email)
            assertEquals("", result.bio)
            assertEquals(null, result.image)

            // Verify that the repository methods were called
            coVerify { userRepository.getUserEntityById(userId.toString()) }
        }

    @Test
    fun testUpdateUser() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val updateUserDto =
                UpdateUserDto(
                    username = "updateduser",
                    email = "updated@example.com",
                    bio = "Updated bio",
                    image = "https://example.com/image.jpg",
                )

            // Create a spy of UserService
            val spyUserService = spyk(userService)

            // Create a mock UserEntity
            val userEntity = mockk<UserEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.randomUUID(), Users)
            every { userEntity.username } returns "testuser"
            every { userEntity.email } returns "test@example.com"
            every { userEntity.bio } returns ""
            every { userEntity.image } returns null
            every { userEntity.salt } returns "salt"
            every { userEntity.hashedPassword } returns "hashedPassword"

            // Mock property setters
            every { userEntity.username = updateUserDto.username!! } returns Unit
            every { userEntity.email = updateUserDto.email!! } returns Unit
            every { userEntity.bio = updateUserDto.bio!! } returns Unit
            every { userEntity.image = updateUserDto.image } returns Unit
            every { userEntity.hashedPassword = any() } returns Unit

            // Create a UserDto from the updated entity
            val updatedUserDto =
                UserDto(
                    email = updateUserDto.email!!,
                    token = "",
                    username = updateUserDto.username!!,
                    bio = updateUserDto.bio!!,
                    image = updateUserDto.image,
                )

            // Mock the UserRepository methods
            coEvery { userRepository.getUserEntityById(userId) } returns userEntity
            coEvery { userRepository.findUserEntityByEmail(updateUserDto.email!!) } returns null
            coEvery {
                userRepository.updateUser(
                    userEntity,
                    updateUserDto.username!!,
                    updateUserDto.email!!,
                    any(),
                    any(),
                    updateUserDto.bio!!,
                    updateUserDto.image,
                )
            } returns updatedUserDto

            // When
            val result = userService.updateUser(userId, updateUserDto)

            // Then
            assertEquals(updateUserDto.username, result.username)
            assertEquals(updateUserDto.email, result.email)
            assertEquals(updateUserDto.bio, result.bio)
            assertEquals(updateUserDto.image, result.image)

            // Verify that the repository methods were called
            coVerify { userRepository.getUserEntityById(userId) }
            coVerify { userRepository.findUserEntityByEmail(updateUserDto.email!!) }
            coVerify {
                userRepository.updateUser(
                    userEntity,
                    updateUserDto.username!!,
                    updateUserDto.email!!,
                    any(),
                    any(),
                    updateUserDto.bio!!,
                    updateUserDto.image,
                )
            }
        }
}
