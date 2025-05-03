package me.bossm0n5t3r.profiles

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.users.UserEntity
import me.bossm0n5t3r.users.Users
import me.bossm0n5t3r.users.UsersRepository
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfilesServiceMockTest {
    private val usersRepository: UsersRepository = mockk(relaxed = true)
    private val followingsRepository: FollowingsRepository = mockk(relaxed = true)
    private val profilesService: ProfilesService = ProfilesService(usersRepository, followingsRepository)

    @Test
    fun testGetProfileWhenNotFollowing() =
        runBlocking {
            // Given
            val username = "testuser"
            val currentUserId = UUID.randomUUID().toString()
            val userId = UUID.randomUUID()
            val currentUserUUID = UUID.fromString(currentUserId)

            val userEntity = mockk<UserEntity>()
            val currentUserEntity = mockk<UserEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(userId, Users)
            every { userEntity.username } returns username
            every { userEntity.bio } returns "Test bio"
            every { userEntity.image } returns "https://example.com/image.jpg"

            every { currentUserEntity.id } returns EntityID(currentUserUUID, Users)

            // Mock the repository methods
            coEvery { usersRepository.findUserEntityByUsername(username) } returns userEntity
            coEvery { usersRepository.getUserEntityById(currentUserId) } returns currentUserEntity
            coEvery { followingsRepository.isFollowing(userId.toString(), currentUserId) } returns false

            // When
            val profileResponse = profilesService.getProfile(username, currentUserId)

            // Then
            assertNotNull(profileResponse.profile)
            assertEquals(username, profileResponse.profile.username)
            assertEquals("Test bio", profileResponse.profile.bio)
            assertEquals("https://example.com/image.jpg", profileResponse.profile.image)
            assertFalse(profileResponse.profile.following)

            // Verify that the repository methods were called
            coVerify { usersRepository.findUserEntityByUsername(username) }
            coVerify { usersRepository.getUserEntityById(currentUserId) }
            coVerify { followingsRepository.isFollowing(userId.toString(), currentUserId) }
        }

    @Test
    fun testGetProfileWhenFollowing() =
        runBlocking {
            // Given
            val username = "testuser"
            val currentUserId = UUID.randomUUID().toString()
            val userId = UUID.randomUUID()
            val currentUserUUID = UUID.fromString(currentUserId)

            val userEntity = mockk<UserEntity>()
            val currentUserEntity = mockk<UserEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(userId, Users)
            every { userEntity.username } returns username
            every { userEntity.bio } returns "Test bio"
            every { userEntity.image } returns "https://example.com/image.jpg"

            every { currentUserEntity.id } returns EntityID(currentUserUUID, Users)

            // Mock the repository methods
            coEvery { usersRepository.findUserEntityByUsername(username) } returns userEntity
            coEvery { usersRepository.getUserEntityById(currentUserId) } returns currentUserEntity
            coEvery { followingsRepository.isFollowing(userId.toString(), currentUserId) } returns true

            // When
            val profileResponse = profilesService.getProfile(username, currentUserId)

            // Then
            assertNotNull(profileResponse.profile)
            assertEquals(username, profileResponse.profile.username)
            assertEquals("Test bio", profileResponse.profile.bio)
            assertEquals("https://example.com/image.jpg", profileResponse.profile.image)
            assertTrue(profileResponse.profile.following)

            // Verify that the repository methods were called
            coVerify { usersRepository.findUserEntityByUsername(username) }
            coVerify { usersRepository.getUserEntityById(currentUserId) }
            coVerify { followingsRepository.isFollowing(userId.toString(), currentUserId) }
        }

    @Test
    fun testGetProfileWithoutCurrentUser() =
        runBlocking {
            // Given
            val username = "testuser"
            val userId = UUID.randomUUID()
            val userEntity = mockk<UserEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(userId, Users)
            every { userEntity.username } returns username
            every { userEntity.bio } returns "Test bio"
            every { userEntity.image } returns "https://example.com/image.jpg"

            // Mock the repository methods
            coEvery { usersRepository.findUserEntityByUsername(username) } returns userEntity

            // When
            val profileResponse = profilesService.getProfile(username, null)

            // Then
            assertNotNull(profileResponse.profile)
            assertEquals(username, profileResponse.profile.username)
            assertEquals("Test bio", profileResponse.profile.bio)
            assertEquals("https://example.com/image.jpg", profileResponse.profile.image)
            assertFalse(profileResponse.profile.following)

            // Verify that the repository methods were called
            coVerify { usersRepository.findUserEntityByUsername(username) }
            coVerify(exactly = 0) { usersRepository.getUserEntityById(any()) }
            coVerify(exactly = 0) { followingsRepository.isFollowing(any(), any()) }
        }

    @Test
    fun testGetProfileWithNonExistentUsername() =
        runBlocking {
            // Given
            val username = "nonexistentuser"
            val currentUserId = UUID.randomUUID().toString()

            // Mock the repository methods
            coEvery { usersRepository.findUserEntityByUsername(username) } returns null

            // When
            val profileResponse = profilesService.getProfile(username, currentUserId)

            // Then
            assertNull(profileResponse.profile)

            // Verify that the repository methods were called
            coVerify { usersRepository.findUserEntityByUsername(username) }
            coVerify(exactly = 0) { usersRepository.getUserEntityById(any()) }
            coVerify(exactly = 0) { followingsRepository.isFollowing(any(), any()) }
        }

    @Test
    fun testFollow() =
        runBlocking {
            // Given
            val username = "testuser"
            val currentUserId = UUID.randomUUID().toString()
            val userId = UUID.randomUUID()
            val userEntity = mockk<UserEntity>()
            val currentUserEntity = mockk<UserEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(userId, Users)
            every { userEntity.username } returns username
            every { userEntity.bio } returns "Test bio"
            every { userEntity.image } returns "https://example.com/image.jpg"

            every { currentUserEntity.id } returns EntityID(UUID.fromString(currentUserId), Users)

            // Mock the repository methods
            coEvery { usersRepository.findUserEntityByUsername(username) } returns userEntity
            coEvery { usersRepository.getUserEntityById(currentUserId) } returns currentUserEntity
            coEvery { followingsRepository.addFollowing(userId.toString(), currentUserId) } returns mockk()

            // When
            val profileResponse = profilesService.follow(username, currentUserId)

            // Then
            assertNotNull(profileResponse.profile)
            assertEquals(username, profileResponse.profile.username)
            assertEquals("Test bio", profileResponse.profile.bio)
            assertEquals("https://example.com/image.jpg", profileResponse.profile.image)
            assertTrue(profileResponse.profile.following)

            // Verify that the repository methods were called
            coVerify { usersRepository.findUserEntityByUsername(username) }
            coVerify { usersRepository.getUserEntityById(currentUserId) }
            coVerify { followingsRepository.addFollowing(userId.toString(), currentUserId) }
        }

    @Test
    fun testFollowNonExistentUser() =
        runBlocking {
            // Given
            val username = "nonexistentuser"
            val currentUserId = UUID.randomUUID().toString()

            // Mock the repository methods
            coEvery { usersRepository.findUserEntityByUsername(username) } returns null

            // When/Then
            assertFailsWith<IllegalStateException> {
                profilesService.follow(username, currentUserId)
            }

            // Verify that the repository methods were called
            coVerify { usersRepository.findUserEntityByUsername(username) }
            coVerify(exactly = 0) { usersRepository.getUserEntityById(any()) }
            coVerify(exactly = 0) { followingsRepository.addFollowing(any(), any()) }
        }

    @Test
    fun testUnfollow() =
        runBlocking {
            // Given
            val username = "testuser"
            val currentUserId = UUID.randomUUID().toString()
            val userId = UUID.randomUUID()
            val userEntity = mockk<UserEntity>()
            val currentUserEntity = mockk<UserEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(userId, Users)
            every { userEntity.username } returns username
            every { userEntity.bio } returns "Test bio"
            every { userEntity.image } returns "https://example.com/image.jpg"

            every { currentUserEntity.id } returns EntityID(UUID.fromString(currentUserId), Users)

            // Mock the repository methods
            coEvery { usersRepository.findUserEntityByUsername(username) } returns userEntity
            coEvery { usersRepository.getUserEntityById(currentUserId) } returns currentUserEntity
            coEvery { followingsRepository.deleteFollowing(userId.toString(), currentUserId) } returns Unit

            // When
            val profileResponse = profilesService.unfollow(username, currentUserId)

            // Then
            assertNotNull(profileResponse.profile)
            assertEquals(username, profileResponse.profile.username)
            assertEquals("Test bio", profileResponse.profile.bio)
            assertEquals("https://example.com/image.jpg", profileResponse.profile.image)
            assertFalse(profileResponse.profile.following)

            // Verify that the repository methods were called
            coVerify { usersRepository.findUserEntityByUsername(username) }
            coVerify { usersRepository.getUserEntityById(currentUserId) }
            coVerify { followingsRepository.deleteFollowing(userId.toString(), currentUserId) }
        }

    @Test
    fun testUnfollowNonExistentUser() =
        runBlocking {
            // Given
            val username = "nonexistentuser"
            val currentUserId = UUID.randomUUID().toString()

            // Mock the repository methods
            coEvery { usersRepository.findUserEntityByUsername(username) } returns null

            // When/Then
            assertFailsWith<IllegalStateException> {
                profilesService.unfollow(username, currentUserId)
            }

            // Verify that the repository methods were called
            coVerify { usersRepository.findUserEntityByUsername(username) }
            coVerify(exactly = 0) { usersRepository.getUserEntityById(any()) }
            coVerify(exactly = 0) { followingsRepository.deleteFollowing(any(), any()) }
        }
}
