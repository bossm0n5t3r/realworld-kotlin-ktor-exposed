package me.bossm0n5t3r.tags

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TagsServiceIntegrationTest {
    private val databaseManager: DatabaseManager = DatabaseManagerImpl()
    private val tagsRepository: TagsRepository = TagsRepository(databaseManager)
    private val tagsService: TagsService = TagsService(tagsRepository)

    @BeforeTest
    fun setup() =
        runBlocking {
            databaseManager.createTables() // Create tables before each test

            // Create some test tags
            val tagNames = listOf("kotlin", "ktor", "exposed", "realworld")
            tagNames.forEach { tagsRepository.getOrCreateTag(it) }
        }

    @AfterTest
    fun tearDown() = databaseManager.dropTables() // Drop tables after each test

    @Test
    fun testGetAllTags() =
        runBlocking {
            // Given
            // Tags are created in setup

            // When
            val result = tagsService.getAllTags()

            // Then
            val expectedTags = listOf("kotlin", "ktor", "exposed", "realworld")
            assertEquals(expectedTags.size, result.tags.size)
            expectedTags.forEach { tagName ->
                assertTrue(result.tags.contains(tagName), "Expected tag '$tagName' not found in result")
            }
        }

    @Test
    fun testGetAllTagsAfterAddingNewTag() =
        runBlocking {
            // Given
            // Initial tags are created in setup
            val newTagName = "newTag"
            tagsRepository.getOrCreateTag(newTagName)

            // When
            val result = tagsService.getAllTags()

            // Then
            val expectedTags = listOf("kotlin", "ktor", "exposed", "realworld", newTagName)
            assertEquals(expectedTags.size, result.tags.size)
            expectedTags.forEach { tagName ->
                assertTrue(result.tags.contains(tagName), "Expected tag '$tagName' not found in result")
            }
        }
}
