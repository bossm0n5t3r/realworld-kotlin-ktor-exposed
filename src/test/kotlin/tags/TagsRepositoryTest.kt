package me.bossm0n5t3r.tags

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.articles.ArticleEntity
import me.bossm0n5t3r.articles.ArticlesRepository
import me.bossm0n5t3r.articles.CreateArticleDto
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
import me.bossm0n5t3r.users.UserEntity
import me.bossm0n5t3r.users.UsersRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TagsRepositoryTest {
    private val databaseManager: DatabaseManager = DatabaseManagerImpl()
    private val tagsRepository: TagsRepository = TagsRepository(databaseManager)
    private val articlesRepository: ArticlesRepository = ArticlesRepository(databaseManager)
    private val usersRepository: UsersRepository = UsersRepository(databaseManager)

    private lateinit var userEntity: UserEntity
    private lateinit var articleEntity: ArticleEntity

    @BeforeTest
    fun setup() =
        runBlocking {
            databaseManager.createTables() // Create tables before each test

            // Create test user
            userEntity = usersRepository.findUserEntityByEmail("user@example.com")
                ?: usersRepository
                    .createUser("user", "user@example.com", "password", "salt")
                    .let { usersRepository.findUserEntityByEmail("user@example.com")!! }

            // Create a test article
            val createArticleDto =
                CreateArticleDto(
                    title = "Test Article",
                    description = "This is a test article",
                    body = "This is the body of the test article",
                )
            articleEntity = articlesRepository.createArticle(userEntity, createArticleDto)
        }

    @AfterTest
    fun tearDown() = databaseManager.dropTables() // Drop tables after each test

    @Test
    fun testGetOrCreateTag_CreateNewTag() =
        runBlocking {
            // Given
            val tagName = "newtag"

            // When
            val tagEntity = tagsRepository.getOrCreateTag(tagName)

            // Then
            assertNotNull(tagEntity)
            assertEquals(tagName, tagEntity.tagName)
        }

    @Test
    fun testGetOrCreateTag_GetExistingTag() =
        runBlocking {
            // Given
            val tagName = "existingtag"
            val createdTagEntity = tagsRepository.getOrCreateTag(tagName)

            // When
            val retrievedTagEntity = tagsRepository.getOrCreateTag(tagName)

            // Then
            assertNotNull(retrievedTagEntity)
            assertEquals(createdTagEntity.id, retrievedTagEntity.id)
            assertEquals(tagName, retrievedTagEntity.tagName)
        }

    @Test
    fun testCreateArticleTagEntity() =
        runBlocking {
            // Given
            val tagName = "testtag"
            val tagEntity = tagsRepository.getOrCreateTag(tagName)

            // When
            val articleTagEntity = tagsRepository.createArticleTagEntity(articleEntity, tagEntity)

            // Then
            assertNotNull(articleTagEntity)
            assertEquals(articleEntity.id, articleTagEntity.articleId)
            assertEquals(tagEntity.id, articleTagEntity.tagId)
        }

    @Test
    fun testCreateMultipleArticleTagEntities() =
        runBlocking {
            // Given
            val tagNames = listOf("tag1", "tag2", "tag3")
            val tagEntities = tagNames.map { tagsRepository.getOrCreateTag(it) }

            // When
            val articleTagEntities =
                tagEntities.map {
                    tagsRepository.createArticleTagEntity(articleEntity, it)
                }

            // Then
            assertEquals(tagNames.size, articleTagEntities.size)

            // Verify each article-tag relationship
            for (i in tagNames.indices) {
                assertEquals(articleEntity.id, articleTagEntities[i].articleId)
                assertEquals(tagEntities[i].id, articleTagEntities[i].tagId)
            }
        }

    @Test
    fun testGetAllTags() =
        runBlocking {
            // Given
            val tagNames = listOf("realworld", "kotlin", "ktor", "exposed")
            tagNames.forEach { tagsRepository.getOrCreateTag(it) }

            // When
            val allTags = tagsRepository.getAllTags()

            // Then
            assertEquals(tagNames.size, allTags.size)
            tagNames.forEach { tagName ->
                assert(allTags.contains(tagName)) { "Expected tag '$tagName' not found in result" }
            }
        }

    @Test
    fun testGetTagOrNull_ExistingTag() =
        runBlocking {
            // Given
            val tagName = "existingtag"
            tagsRepository.getOrCreateTag(tagName)

            // When
            val tagEntity = tagsRepository.getTagOrNull(tagName)

            // Then
            assertNotNull(tagEntity)
            assertEquals(tagName, tagEntity.tagName)
        }

    @Test
    fun testGetTagOrNull_NonExistingTag() =
        runBlocking {
            // Given
            val tagName = "nonexistingtag"

            // When
            val tagEntity = tagsRepository.getTagOrNull(tagName)

            // Then
            assertEquals(null, tagEntity)
        }

    @Test
    fun testGetAllTagsByArticle() =
        runBlocking {
            // Given
            val tagNames = listOf("tag1", "tag2", "tag3")
            val tagEntities = tagNames.map { tagsRepository.getOrCreateTag(it) }

            // Create article-tag relationships
            tagEntities.forEach { tagsRepository.createArticleTagEntity(articleEntity, it) }

            // When
            val retrievedTags = tagsRepository.getAllTagsByArticle(articleEntity)

            // Then
            assertEquals(tagNames.size, retrievedTags.size)
            tagEntities.forEach { tagEntity ->
                assert(retrievedTags.any { it.id == tagEntity.id }) { "Expected tag '${tagEntity.tagName}' not found in result" }
            }
        }

    @Test
    fun testIsArticleHasTag_True() =
        runBlocking {
            // Given
            val tagName = "testtag"
            val tagEntity = tagsRepository.getOrCreateTag(tagName)
            tagsRepository.createArticleTagEntity(articleEntity, tagEntity)

            // When
            val hasTag = tagsRepository.isArticleHasTag(articleEntity, tagEntity)

            // Then
            assertEquals(true, hasTag)
        }

    @Test
    fun testIsArticleHasTag_False() =
        runBlocking {
            // Given
            val tagName = "nonassociatedtag"
            val tagEntity = tagsRepository.getOrCreateTag(tagName)
            // Not creating an article-tag relationship

            // When
            val hasTag = tagsRepository.isArticleHasTag(articleEntity, tagEntity)

            // Then
            assertEquals(false, hasTag)
        }
}
