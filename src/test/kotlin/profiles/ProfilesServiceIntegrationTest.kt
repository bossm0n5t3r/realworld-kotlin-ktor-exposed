package me.bossm0n5t3r.profiles

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
import me.bossm0n5t3r.users.UsersRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfilesServiceIntegrationTest {
    private val databaseManager: DatabaseManager = DatabaseManagerImpl()
    private val usersRepository: UsersRepository = UsersRepository(databaseManager)
    private val followingsRepository: FollowingsRepository = FollowingsRepository(databaseManager)
    private val profilesService: ProfilesService = ProfilesService(usersRepository, followingsRepository)

    private lateinit var user1Id: String
    private lateinit var user2Id: String
    private lateinit var user1Username: String
    private lateinit var user2Username: String

    @BeforeTest
    fun setup() =
        runBlocking {
            databaseManager.createTables() // Create tables before each test

            // Create test users
            user1Username = "user1"
            user2Username = "user2"
            usersRepository.createUser(user1Username, "user1@example.com", "password1", "salt1")
            usersRepository.createUser(user2Username, "user2@example.com", "password2", "salt2")

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
    fun testGetProfileWhenNotFollowing() =
        runBlocking {
            // Given
            // user1 and user2 are created in setup
            // user1 is not following user2

            // When
            val profileResponse = profilesService.getProfile(user2Username, user1Id)

            // Then
            assertNotNull(profileResponse.profile)
            assertEquals(user2Username, profileResponse.profile.username)
            assertFalse(profileResponse.profile.following)
        }

    @Test
    fun testGetProfileWhenFollowing() =
        runBlocking {
            // Given
            // user1 and user2 are created in setup
            // Make user1 follow user2
            followingsRepository.addFollowing(user2Id, user1Id)

            // When
            val profileResponse = profilesService.getProfile(user2Username, user1Id)

            // Then
            assertNotNull(profileResponse.profile)
            assertEquals(user2Username, profileResponse.profile.username)
            assertTrue(profileResponse.profile.following)
        }

    @Test
    fun testGetProfileWithoutCurrentUser() =
        runBlocking {
            // Given
            // user2 is created in setup

            // When
            val profileResponse = profilesService.getProfile(user2Username, null)

            // Then
            assertNotNull(profileResponse.profile)
            assertEquals(user2Username, profileResponse.profile.username)
            assertFalse(profileResponse.profile.following)
        }

    @Test
    fun testGetProfileWithNonExistentUsername() =
        runBlocking {
            // Given
            val nonExistentUsername = "nonexistentuser"

            // When
            val profileResponse = profilesService.getProfile(nonExistentUsername, user1Id)

            // Then
            assertNull(profileResponse.profile)
        }

    @Test
    fun testFollow() =
        runBlocking {
            // Given
            // user1 and user2 are created in setup

            // When
            val profileResponse = profilesService.follow(user2Username, user1Id)

            // Then
            assertNotNull(profileResponse.profile)
            assertEquals(user2Username, profileResponse.profile.username)
            assertTrue(profileResponse.profile.following)

            // Verify that the following relationship was created in the database
            assertTrue(followingsRepository.isFollowing(user2Id, user1Id))
        }

    @Test
    fun testFollowNonExistentUser() =
        runBlocking<Unit> {
            // Given
            val nonExistentUsername = "nonexistentuser"

            // When/Then
            assertFailsWith<IllegalStateException> {
                profilesService.follow(nonExistentUsername, user1Id)
            }
        }

    @Test
    fun testUnfollow() =
        runBlocking {
            // Given
            // user1 and user2 are created in setup
            // Make user1 follow user2
            followingsRepository.addFollowing(user2Id, user1Id)
            assertTrue(followingsRepository.isFollowing(user2Id, user1Id))

            // When
            val profileResponse = profilesService.unfollow(user2Username, user1Id)

            // Then
            assertNotNull(profileResponse.profile)
            assertEquals(user2Username, profileResponse.profile.username)
            assertFalse(profileResponse.profile.following)

            // Verify that the following relationship was removed from the database
            assertFalse(followingsRepository.isFollowing(user2Id, user1Id))
        }

    @Test
    fun testUnfollowNonExistentUser() =
        runBlocking<Unit> {
            // Given
            val nonExistentUsername = "nonexistentuser"

            // When/Then
            assertFailsWith<IllegalStateException> {
                profilesService.unfollow(nonExistentUsername, user1Id)
            }
        }
}
