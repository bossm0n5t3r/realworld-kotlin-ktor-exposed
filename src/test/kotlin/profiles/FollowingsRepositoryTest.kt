package me.bossm0n5t3r.profiles

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
import me.bossm0n5t3r.users.UsersRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FollowingsRepositoryTest {
    private val databaseManager: DatabaseManager = DatabaseManagerImpl()
    private val followingsRepository: FollowingsRepository = FollowingsRepository(databaseManager)
    private val usersRepository: UsersRepository = UsersRepository(databaseManager)

    private lateinit var user1Id: String
    private lateinit var user2Id: String

    @BeforeTest
    fun setup() =
        runBlocking {
            databaseManager.createTables() // Create tables before each test

            // Create test users
            usersRepository.createUser("user1", "user1@example.com", "password1", "salt1")
            usersRepository.createUser("user2", "user2@example.com", "password2", "salt2")

            // Get user IDs
            user1Id = usersRepository
                .findUserEntityByEmail("user1@example.com")
                ?.id
                ?.value
                ?.toString() ?: error("User not found")
            user2Id = usersRepository
                .findUserEntityByEmail("user2@example.com")
                ?.id
                ?.value
                ?.toString() ?: error("User not found")
        }

    @AfterTest
    fun tearDown() = databaseManager.dropTables() // Drop tables after each test

    @Test
    fun testAddFollowing() =
        runBlocking {
            // Given
            // user1 and user2 are created in setup

            // When
            val following = followingsRepository.addFollowing(user1Id, user2Id)

            // Then
            assertNotNull(following)
            assertEquals(user1Id, following.userId.toString())
            assertEquals(user2Id, following.followerId.toString())
        }

    @Test
    fun testIsFollowing() =
        runBlocking {
            // Given
            // user1 and user2 are created in setup
            followingsRepository.addFollowing(user1Id, user2Id)

            // When
            val isFollowing = followingsRepository.isFollowing(user1Id, user2Id)
            val isNotFollowing = followingsRepository.isFollowing(user2Id, user1Id)

            // Then
            assertTrue(isFollowing)
            assertFalse(isNotFollowing)
        }

    @Test
    fun testGetAllFollowingsByUserId() =
        runBlocking {
            // Given
            // user1 and user2 are created in setup
            // Create a third user
            usersRepository.createUser("user3", "user3@example.com", "password3", "salt3")
            val user3Id =
                usersRepository
                    .findUserEntityByEmail("user3@example.com")
                    ?.id
                    ?.value
                    ?.toString() ?: error("User not found")

            // user1 follows user2 and user3
            followingsRepository.addFollowing(user1Id, user2Id)
            followingsRepository.addFollowing(user1Id, user3Id)

            // When
            val followings = followingsRepository.getAllFollowingsByUserId(user1Id)

            // Then
            assertEquals(2, followings.size)
            assertTrue(followings.any { it.followerId.toString() == user2Id })
            assertTrue(followings.any { it.followerId.toString() == user3Id })
        }

    @Test
    fun testDeleteFollowing() =
        runBlocking {
            // Given
            // user1 and user2 are created in setup
            followingsRepository.addFollowing(user1Id, user2Id)
            assertTrue(followingsRepository.isFollowing(user1Id, user2Id))

            // When
            followingsRepository.deleteFollowing(user1Id, user2Id)

            // Then
            assertFalse(followingsRepository.isFollowing(user1Id, user2Id))
        }
}
