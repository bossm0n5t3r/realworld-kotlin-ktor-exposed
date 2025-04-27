package me.bossm0n5t3r.users

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
import me.bossm0n5t3r.uitilities.PasswordEncoder
import me.bossm0n5t3r.uitilities.PasswordEncoderImpl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserServiceIntegrationTest {
    private val databaseManager: DatabaseManager = DatabaseManagerImpl()
    private val passwordEncoder: PasswordEncoder = PasswordEncoderImpl()
    private val userService: UserService = UserService(databaseManager, passwordEncoder)

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

    /**
     * Helper method to register a user and return their ID
     */
    private suspend fun registerUserAndGetId(
        username: String = "testuser",
        email: String = "test@example.com",
        password: String = "password123",
    ): String {
        // Register the user
        val createUserDto =
            CreateUserDto(
                username = username,
                email = email,
                password = password,
            )
        userService.register(createUserDto)

        // Get the user's ID by querying the database
        return databaseManager.dbQuery {
            val userEntity = UserEntity.find { Users.email eq email }.first()
            userEntity.id.value.toString()
        }
    }

    @Test
    fun testRegister() =
        runBlocking {
            // Given a valid CreateUserDto
            val createUserDto =
                CreateUserDto(
                    username = "testuser",
                    email = "test@example.com",
                    password = "password123",
                )

            // When registering a new user
            val userDto = userService.register(createUserDto)

            // Then the returned UserDto should have the correct values
            assertEquals(createUserDto.username, userDto.username)
            assertEquals(createUserDto.email, userDto.email)
            assertEquals("", userDto.bio)
            assertEquals(null, userDto.image)
        }

    @Test
    fun testRegisterWithDuplicateUsername() =
        runBlocking {
            // Given a user already registered
            val createUserDto =
                CreateUserDto(
                    username = "testuser",
                    email = "test@example.com",
                    password = "password123",
                )
            userService.register(createUserDto)

            // When trying to register another user with the same username but different email
            val duplicateUsernameDto =
                CreateUserDto(
                    username = "testuser",
                    email = "different@example.com",
                    password = "password123",
                )

            // Then an exception should be thrown
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    userService.register(duplicateUsernameDto)
                }
            assertEquals("User already registered", exception.message)
        }

    @Test
    fun testRegisterWithDuplicateEmail() =
        runBlocking {
            // Given a user already registered
            val createUserDto =
                CreateUserDto(
                    username = "testuser",
                    email = "test@example.com",
                    password = "password123",
                )
            userService.register(createUserDto)

            // When trying to register another user with the same email but different username
            val duplicateEmailDto =
                CreateUserDto(
                    username = "differentuser",
                    email = "test@example.com",
                    password = "password123",
                )

            // Then an exception should be thrown
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    userService.register(duplicateEmailDto)
                }
            assertEquals("User already registered", exception.message)
        }

    @Test
    fun testLogin() =
        runBlocking {
            // Given a registered user
            val createUserDto =
                CreateUserDto(
                    username = "testuser",
                    email = "test@example.com",
                    password = "password123",
                )
            userService.register(createUserDto)

            // When logging in with correct credentials
            val loginUserDto =
                LoginUserDto(
                    email = "test@example.com",
                    password = "password123",
                )
            val userDto = userService.login(loginUserDto)

            // Then the returned UserDto should have the correct values
            assertEquals(createUserDto.username, userDto.username)
            assertEquals(createUserDto.email, userDto.email)
        }

    @Test
    fun testLoginWithInvalidEmail() =
        runBlocking {
            // Given a registered user
            val createUserDto =
                CreateUserDto(
                    username = "testuser",
                    email = "test@example.com",
                    password = "password123",
                )
            userService.register(createUserDto)

            // When logging in with an invalid email
            val loginUserDto =
                LoginUserDto(
                    email = "wrong@example.com",
                    password = "password123",
                )

            // Then an exception should be thrown
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    userService.login(loginUserDto)
                }
            assertEquals("Not found User", exception.message)
        }

    @Test
    fun testLoginWithInvalidPassword() =
        runBlocking {
            // Given a registered user
            val createUserDto =
                CreateUserDto(
                    username = "testuser",
                    email = "test@example.com",
                    password = "password123",
                )
            userService.register(createUserDto)

            // When logging in with an invalid password
            val loginUserDto =
                LoginUserDto(
                    email = "test@example.com",
                    password = "wrongpassword",
                )

            // Then an exception should be thrown
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    userService.login(loginUserDto)
                }
            assertEquals("Invalid password", exception.message)
        }

    @Test
    fun testGetUserById() =
        runBlocking {
            // Given a registered user with a known ID
            val userId = registerUserAndGetId()

            // When getting the user by ID
            val userDto = userService.getUserById(userId)

            // Then the returned UserDto should have the correct values
            assertEquals("testuser", userDto.username)
            assertEquals("test@example.com", userDto.email)
        }

    @Test
    fun testGetAllUsers() =
        runBlocking {
            // Given multiple registered users
            val user1 =
                CreateUserDto(
                    username = "user1",
                    email = "user1@example.com",
                    password = "password123",
                )
            val user2 =
                CreateUserDto(
                    username = "user2",
                    email = "user2@example.com",
                    password = "password123",
                )
            userService.register(user1)
            userService.register(user2)

            // When getting all users
            val allUsers = userService.getAllUsers()

            // Then the returned list should contain both users
            assertEquals(2, allUsers.size)
            assertTrue(allUsers.any { it.username == user1.username && it.email == user1.email })
            assertTrue(allUsers.any { it.username == user2.username && it.email == user2.email })
        }

    @Test
    fun testUpdateUser() =
        runBlocking {
            // Given a user with known credentials
            val username = "testuser"
            val email = "test@example.com"
            val password = "password123"

            // Register the user
            val createUserDto =
                CreateUserDto(
                    username = username,
                    email = email,
                    password = password,
                )
            userService.register(createUserDto)

            // Get the user's ID
            val userId =
                databaseManager.dbQuery {
                    val userEntity = UserEntity.find { Users.email eq email }.first()
                    userEntity.id.value.toString()
                }

            // When updating the user
            val updateUserDto =
                UpdateUserDto(
                    username = "updateduser",
                    email = "updated@example.com",
                    bio = "Updated bio",
                    image = "https://example.com/image.jpg",
                )

            // Wrap the updateUser call in a transaction
            val updatedUser =
                databaseManager.dbQuery {
                    userService.updateUser(userId, updateUserDto)
                }

            // Then the returned UserDto should have the updated values
            assertEquals(updateUserDto.username, updatedUser.username)
            assertEquals(updateUserDto.email, updatedUser.email)
            assertEquals(updateUserDto.bio, updatedUser.bio)
            assertEquals(updateUserDto.image, updatedUser.image)
        }

    @Test
    fun testUpdateUserPassword() =
        runBlocking<Unit> {
            // Given a user with known credentials
            val username = "testuser"
            val email = "test@example.com"
            val password = "password123"

            // Register the user
            val createUserDto =
                CreateUserDto(
                    username = username,
                    email = email,
                    password = password,
                )
            userService.register(createUserDto)

            // Get the user's ID
            val userId =
                databaseManager.dbQuery {
                    val userEntity = UserEntity.find { Users.email eq email }.first()
                    userEntity.id.value.toString()
                }

            // When updating the user's password
            val updateUserDto =
                UpdateUserDto(
                    password = "newpassword123",
                )

            // Wrap the updateUser call in a transaction
            databaseManager.dbQuery {
                userService.updateUser(userId, updateUserDto)
            }

            // Then logging in with the old password should fail
            val oldLoginDto =
                LoginUserDto(
                    email = email,
                    password = password,
                )
            assertFailsWith<IllegalArgumentException> {
                userService.login(oldLoginDto)
            }

            // And logging in with the new password should succeed
            val newLoginDto =
                LoginUserDto(
                    email = email,
                    password = "newpassword123",
                )
            val loggedInUser = userService.login(newLoginDto)
            assertNotNull(loggedInUser)
        }

    @Test
    fun testUpdateUserBioOnly() =
        runBlocking {
            // Given a user with known credentials
            val username = "testuser"
            val email = "test@example.com"
            val password = "password123"

            // Register the user
            val createUserDto =
                CreateUserDto(
                    username = username,
                    email = email,
                    password = password,
                )
            val originalUser = userService.register(createUserDto)

            // Get the user's ID
            val userId =
                databaseManager.dbQuery {
                    val userEntity = UserEntity.find { Users.email eq email }.first()
                    userEntity.id.value.toString()
                }

            // When updating only the user's bio
            val newBio = "This is my updated bio"
            val updateUserDto =
                UpdateUserDto(
                    bio = newBio,
                )

            // Wrap the updateUser call in a transaction
            val updatedUser =
                databaseManager.dbQuery {
                    userService.updateUser(userId, updateUserDto)
                }

            // Then only the bio should be updated, other fields should remain the same
            assertEquals(newBio, updatedUser.bio)
            assertEquals(originalUser.username, updatedUser.username)
            assertEquals(originalUser.email, updatedUser.email)
            assertEquals(originalUser.image, updatedUser.image)
        }

    @Test
    fun testUpdateUserImageOnly() =
        runBlocking {
            // Given a user with known credentials
            val username = "testuser"
            val email = "test@example.com"
            val password = "password123"

            // Register the user
            val createUserDto =
                CreateUserDto(
                    username = username,
                    email = email,
                    password = password,
                )
            val originalUser = userService.register(createUserDto)

            // Get the user's ID
            val userId =
                databaseManager.dbQuery {
                    val userEntity = UserEntity.find { Users.email eq email }.first()
                    userEntity.id.value.toString()
                }

            // When updating only the user's image
            val newImage = "https://example.com/new-image.jpg"
            val updateUserDto =
                UpdateUserDto(
                    image = newImage,
                )

            // Wrap the updateUser call in a transaction
            val updatedUser =
                databaseManager.dbQuery {
                    userService.updateUser(userId, updateUserDto)
                }

            // Then only the image should be updated, other fields should remain the same
            assertEquals(newImage, updatedUser.image)
            assertEquals(originalUser.username, updatedUser.username)
            assertEquals(originalUser.email, updatedUser.email)
            assertEquals(originalUser.bio, updatedUser.bio)
        }

    @Test
    fun testUpdateUserWithDuplicateUsername() =
        runBlocking {
            // Given two users with different usernames
            val user1 =
                userService.register(
                    CreateUserDto(
                        username = "user1",
                        email = "user1@example.com",
                        password = "password123",
                    ),
                )
            val user2 =
                userService.register(
                    CreateUserDto(
                        username = "user2",
                        email = "user2@example.com",
                        password = "password123",
                    ),
                )

            // Get user2's ID
            val user2Id =
                databaseManager.dbQuery {
                    val userEntity = UserEntity.find { Users.email eq "user2@example.com" }.first()
                    userEntity.id.value.toString()
                }

            // When trying to update user2's username to user1's username
            val updateUserDto =
                UpdateUserDto(
                    username = "user1",
                )

            // Then an exception should be thrown
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    databaseManager.dbQuery {
                        userService.updateUser(user2Id, updateUserDto)
                    }
                }
            assertEquals("User already registered", exception.message)
        }

    @Test
    fun testUpdateUserWithDuplicateEmail() =
        runBlocking {
            // Given two users with different emails
            val user1 =
                userService.register(
                    CreateUserDto(
                        username = "user1",
                        email = "user1@example.com",
                        password = "password123",
                    ),
                )
            val user2 =
                userService.register(
                    CreateUserDto(
                        username = "user2",
                        email = "user2@example.com",
                        password = "password123",
                    ),
                )

            // Get user2's ID
            val user2Id =
                databaseManager.dbQuery {
                    val userEntity = UserEntity.find { Users.email eq "user2@example.com" }.first()
                    userEntity.id.value.toString()
                }

            // When trying to update user2's email to user1's email
            val updateUserDto =
                UpdateUserDto(
                    email = "user1@example.com",
                )

            // Then an exception should be thrown
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    databaseManager.dbQuery {
                        userService.updateUser(user2Id, updateUserDto)
                    }
                }
            assertEquals("User already registered", exception.message)
        }

    @Test
    fun testUpdateUserWithInvalidId() =
        runBlocking<Unit> {
            // Given an invalid user ID
            val invalidUserId = "00000000-0000-0000-0000-000000000000"

            // When trying to update a user with an invalid ID
            val updateUserDto =
                UpdateUserDto(
                    bio = "This update should fail",
                )

            // Then an exception should be thrown
            assertFailsWith<IllegalArgumentException> {
                databaseManager.dbQuery {
                    userService.updateUser(invalidUserId, updateUserDto)
                }
            }
        }
}
