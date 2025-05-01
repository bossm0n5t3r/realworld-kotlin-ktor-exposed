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
    private val userRepository: UserRepository = UserRepository(databaseManager)

    @BeforeTest
    fun setup() =
        runBlocking {
            // Create tables before each test
            databaseManager.createTables()
        }

    @AfterTest
    fun tearDown() =
        runBlocking {
            // Drop tables after each test
            databaseManager.dropTables()
        }

    @Test
    fun testCreateUser() =
        runBlocking {
            // Given
            val username = "testuser"
            val email = "test@example.com"
            val hashedPassword = "hashedpassword123"
            val salt = "salt123"

            // When
            val userDto = userRepository.createUser(username, email, hashedPassword, salt)

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
            userRepository.createUser(username, email, hashedPassword, salt)

            // When
            val userEntity = userRepository.findUserEntityByEmail(email)

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
            userRepository.createUser(username, email, hashedPassword, salt)

            // Get the user's ID
            val userId =
                databaseManager.dbQuery {
                    val userEntity = UserEntity.find { Users.email eq email }.first()
                    userEntity.id.value.toString()
                }

            // When
            val userEntity = userRepository.getUserEntityById(userId)

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

            userRepository.createUser(user1Username, user1Email, hashedPassword, salt)
            userRepository.createUser(user2Username, user2Email, hashedPassword, salt)

            // When
            val users = userRepository.getAllUsers()

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
            userRepository.createUser(username, email, hashedPassword, salt)

            // Get the user entity
            val userEntity =
                databaseManager.dbQuery {
                    UserEntity.find { Users.email eq email }.first()
                }

            // When - update the user's bio and image
            val newBio = "This is my updated bio"
            val newImage = "https://example.com/image.jpg"

            databaseManager.dbQuery {
                userEntity.bio = newBio
                userEntity.image = newImage
            }

            val updatedUserDto = userRepository.updateUser(userEntity)

            // Then
            assertEquals(username, updatedUserDto.username)
            assertEquals(email, updatedUserDto.email)
            assertEquals(newBio, updatedUserDto.bio)
            assertEquals(newImage, updatedUserDto.image)
        }
}
