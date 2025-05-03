package me.bossm0n5t3r.users

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserRepositoryTest {
    private val databaseManager: DatabaseManager = DatabaseManagerImpl()
    private val usersRepository: UsersRepository = UsersRepository(databaseManager)

    @BeforeTest
    fun setup() = databaseManager.createTables() // Create tables before each test

    @AfterTest
    fun tearDown() = databaseManager.dropTables() // Drop tables after each test

    @Test
    fun testCreateUser() =
        runBlocking {
            // Given
            val username = "testuser"
            val email = "test@example.com"
            val hashedPassword = "hashedpassword123"
            val salt = "salt123"

            // When
            val userDto = usersRepository.createUser(username, email, hashedPassword, salt)

            // Then
            assertEquals(username, userDto.username)
            assertEquals(email, userDto.email)
            assertEquals("", userDto.bio)
            assertEquals(null, userDto.image)
        }

    @Test
    fun testFindUserEntityByEmail() =
        runBlocking {
            // Given
            val username = "testuser"
            val email = "test@example.com"
            val hashedPassword = "hashedpassword123"
            val salt = "salt123"
            usersRepository.createUser(username, email, hashedPassword, salt)

            // When
            val userEntity = usersRepository.findUserEntityByEmail(email)

            // Then
            assertNotNull(userEntity)
            assertEquals(username, userEntity.username)
            assertEquals(email, userEntity.email)
        }

    @Test
    fun testFindUserEntityByUsername() =
        runBlocking {
            // Given
            val username = "testuser"
            val email = "test@example.com"
            val hashedPassword = "hashedpassword123"
            val salt = "salt123"
            usersRepository.createUser(username, email, hashedPassword, salt)

            // When
            val userEntity = usersRepository.findUserEntityByUsername(username)

            // Then
            assertNotNull(userEntity)
            assertEquals(username, userEntity.username)
            assertEquals(email, userEntity.email)
        }

    @Test
    fun testGetUserEntityById() =
        runBlocking {
            // Given
            val username = "testuser"
            val email = "test@example.com"
            val hashedPassword = "hashedpassword123"
            val salt = "salt123"
            usersRepository.createUser(username, email, hashedPassword, salt)

            // Get the user's ID
            val userId =
                usersRepository
                    .findUserEntityByEmail(email)
                    ?.id
                    ?.value
                    ?.toString() ?: error("User not found")

            // When
            val userEntity = usersRepository.getUserEntityById(userId)

            // Then
            assertNotNull(userEntity)
            assertEquals(username, userEntity.username)
            assertEquals(email, userEntity.email)
            assertEquals(hashedPassword, userEntity.hashedPassword)
            assertEquals(salt, userEntity.salt)
        }

    @Test
    fun testGetAllUsers() =
        runBlocking {
            // Given
            val user1Username = "user1"
            val user1Email = "user1@example.com"
            val user2Username = "user2"
            val user2Email = "user2@example.com"
            val hashedPassword = "hashedpassword123"
            val salt = "salt123"

            usersRepository.createUser(user1Username, user1Email, hashedPassword, salt)
            usersRepository.createUser(user2Username, user2Email, hashedPassword, salt)

            // When
            val users = usersRepository.getAllUsers()

            // Then
            assertEquals(2, users.size)
            assertTrue(users.any { it.username == user1Username && it.email == user1Email })
            assertTrue(users.any { it.username == user2Username && it.email == user2Email })
        }

    @Test
    fun testUpdateUser() =
        runBlocking {
            // Given
            val username = "testuser"
            val email = "test@example.com"
            val hashedPassword = "hashedpassword123"
            val salt = "salt123"
            usersRepository.createUser(username, email, hashedPassword, salt)

            // Get the user entity
            val userEntity = usersRepository.findUserEntityByEmail(email) ?: error("User not found")

            // When - update the user's bio and image
            val newBio = "This is my updated bio"
            val newImage = "https://example.com/image.jpg"

            val updatedUserDto =
                usersRepository.updateUser(
                    userEntity,
                    userEntity.username,
                    userEntity.email,
                    userEntity.hashedPassword,
                    userEntity.salt,
                    newBio,
                    newImage,
                )

            // Then
            assertEquals(username, updatedUserDto.username)
            assertEquals(email, updatedUserDto.email)
            assertEquals(newBio, updatedUserDto.bio)
            assertEquals(newImage, updatedUserDto.image)
        }
}
