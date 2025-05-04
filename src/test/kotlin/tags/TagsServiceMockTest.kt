package me.bossm0n5t3r.tags

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class TagsServiceMockTest {
    private val tagsRepository: TagsRepository = mockk(relaxed = true)
    private val tagsService: TagsService = TagsService(tagsRepository)

    @Test
    fun testGetAllTags() =
        runBlocking {
            // Given
            val tagsList = listOf("kotlin", "ktor", "exposed")
            coEvery { tagsRepository.getAllTags() } returns tagsList

            // When
            val result = tagsService.getAllTags()

            // Then
            assertEquals(TagsDto(tagsList), result)
            coVerify { tagsRepository.getAllTags() }
        }

    @Test
    fun testGetAllTagsWithEmptyList() =
        runBlocking {
            // Given
            val tagsList = emptyList<String>()
            coEvery { tagsRepository.getAllTags() } returns tagsList

            // When
            val result = tagsService.getAllTags()

            // Then
            assertEquals(TagsDto(tagsList), result)
            coVerify { tagsRepository.getAllTags() }
        }
}
