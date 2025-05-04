package me.bossm0n5t3r.articles

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
import me.bossm0n5t3r.users.UserEntity
import me.bossm0n5t3r.users.UsersRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ArticlesRepositoryTest {
    private val databaseManager: DatabaseManager = DatabaseManagerImpl()
    private val articlesRepository: ArticlesRepository = ArticlesRepository(databaseManager)
    private val usersRepository: UsersRepository = UsersRepository(databaseManager)

    private lateinit var userEntity: UserEntity
    private lateinit var anotherUserEntity: UserEntity

    @BeforeTest
    fun setup() =
        runBlocking {
            databaseManager.createTables() // Create tables before each test

            // Create test users
            userEntity = usersRepository.findUserEntityByEmail("user@example.com")
                ?: usersRepository
                    .createUser("user", "user@example.com", "password", "salt")
                    .let { usersRepository.findUserEntityByEmail("user@example.com")!! }

            anotherUserEntity = usersRepository.findUserEntityByEmail("another@example.com")
                ?: usersRepository
                    .createUser("another", "another@example.com", "password", "salt")
                    .let { usersRepository.findUserEntityByEmail("another@example.com")!! }
        }

    @AfterTest
    fun tearDown() = databaseManager.dropTables() // Drop tables after each test

    @Test
    fun testCreateArticle() =
        runBlocking {
            // Given
            val createArticleDto =
                CreateArticleDto(
                    title = "Test Article",
                    description = "This is a test article",
                    body = "This is the body of the test article",
                )

            // When
            val articleEntity = articlesRepository.createArticle(userEntity, createArticleDto)

            // Then
            assertNotNull(articleEntity)
            assertEquals(createArticleDto.title, articleEntity.title)
            assertEquals("test-article", articleEntity.slug) // Assuming toSlug() converts "Test Article" to "test-article"
            assertEquals(createArticleDto.description, articleEntity.description)
            assertEquals(createArticleDto.body, articleEntity.body)
            assertEquals(userEntity.id, articleEntity.authorId)
        }

    @Test
    fun testGetArticleBySlug_ExistingArticle() =
        runBlocking {
            // Given
            val createArticleDto =
                CreateArticleDto(
                    title = "Test Article",
                    description = "This is a test article",
                    body = "This is the body of the test article",
                )
            val createdArticle = articlesRepository.createArticle(userEntity, createArticleDto)
            val slug = createdArticle.slug

            // When
            val retrievedArticle = articlesRepository.getArticleBySlug(slug)

            // Then
            assertNotNull(retrievedArticle)
            assertEquals(createdArticle.id, retrievedArticle.id)
            assertEquals(createdArticle.title, retrievedArticle.title)
            assertEquals(createdArticle.slug, retrievedArticle.slug)
            assertEquals(createdArticle.description, retrievedArticle.description)
            assertEquals(createdArticle.body, retrievedArticle.body)
            assertEquals(createdArticle.authorId, retrievedArticle.authorId)
        }

    @Test
    fun testGetArticleBySlug_NonExistingArticle() =
        runBlocking {
            // Given
            val nonExistingSlug = "non-existing-article"

            // When
            val retrievedArticle = articlesRepository.getArticleBySlug(nonExistingSlug)

            // Then
            assertNull(retrievedArticle)
        }

    @Test
    fun testUpdateArticle() =
        runBlocking {
            // Given
            val createArticleDto =
                CreateArticleDto(
                    title = "Original Title",
                    description = "Original description",
                    body = "Original body",
                )
            val createdArticle = articlesRepository.createArticle(userEntity, createArticleDto)

            val updateArticleDto =
                UpdateArticleDto(
                    title = "Updated Title",
                    description = "Updated description",
                    body = "Updated body",
                )

            // When
            val updatedArticle = articlesRepository.updateArticle(createdArticle, updateArticleDto)

            // Then
            assertNotNull(updatedArticle)
            assertEquals(updateArticleDto.title, updatedArticle.title)
            assertEquals("updated-title", updatedArticle.slug) // Assuming toSlug() converts "Updated Title" to "updated-title"
            assertEquals(updateArticleDto.description, updatedArticle.description)
            assertEquals(updateArticleDto.body, updatedArticle.body)
            assertEquals(createdArticle.authorId, updatedArticle.authorId)
        }

    @Test
    fun testUpdateArticle_PartialUpdate() =
        runBlocking {
            // Given
            val createArticleDto =
                CreateArticleDto(
                    title = "Original Title",
                    description = "Original description",
                    body = "Original body",
                )
            val createdArticle = articlesRepository.createArticle(userEntity, createArticleDto)

            val updateArticleDto =
                UpdateArticleDto(
                    title = "Updated Title",
                    // description and body are null, so they should remain unchanged
                )

            // When
            val updatedArticle = articlesRepository.updateArticle(createdArticle, updateArticleDto)

            // Then
            assertNotNull(updatedArticle)
            assertEquals(updateArticleDto.title, updatedArticle.title)
            assertEquals("updated-title", updatedArticle.slug)
            assertEquals(createdArticle.description, updatedArticle.description) // Should remain unchanged
            assertEquals(createdArticle.body, updatedArticle.body) // Should remain unchanged
            assertEquals(createdArticle.authorId, updatedArticle.authorId)
        }

    @Test
    fun testGetAllArticles_NoFilter() =
        runBlocking {
            // Given
            val articlesToCreate = 5
            repeat(articlesToCreate) { index ->
                val createArticleDto =
                    CreateArticleDto(
                        title = "Test Article $index",
                        description = "Description $index",
                        body = "Body $index",
                    )
                articlesRepository.createArticle(userEntity, createArticleDto)
            }

            // When
            val retrievedArticles = articlesRepository.getAllArticles(limit = 10, offset = 0)

            // Then
            assertEquals(articlesToCreate, retrievedArticles.size)
        }

    @Test
    fun testGetAllArticles_WithAuthorFilter() =
        runBlocking {
            // Given
            // Create articles by first user
            repeat(3) { index ->
                val createArticleDto =
                    CreateArticleDto(
                        title = "User1 Article $index",
                        description = "Description $index",
                        body = "Body $index",
                    )
                articlesRepository.createArticle(userEntity, createArticleDto)
            }

            // Create articles by second user
            repeat(2) { index ->
                val createArticleDto =
                    CreateArticleDto(
                        title = "User2 Article $index",
                        description = "Description $index",
                        body = "Body $index",
                    )
                articlesRepository.createArticle(anotherUserEntity, createArticleDto)
            }

            // When - Get articles by first user
            val user1Articles = articlesRepository.getAllArticles(author = userEntity, limit = 10, offset = 0)

            // Then
            assertEquals(3, user1Articles.size)
            user1Articles.forEach { article ->
                assertEquals(userEntity.id, article.authorId)
            }

            // When - Get articles by second user
            val user2Articles = articlesRepository.getAllArticles(author = anotherUserEntity, limit = 10, offset = 0)

            // Then
            assertEquals(2, user2Articles.size)
            user2Articles.forEach { article ->
                assertEquals(anotherUserEntity.id, article.authorId)
            }
        }

    @Test
    fun testGetAllArticles_WithPagination() =
        runBlocking {
            // Given
            val totalArticles = 10
            repeat(totalArticles) { index ->
                val createArticleDto =
                    CreateArticleDto(
                        title = "Test Article $index",
                        description = "Description $index",
                        body = "Body $index",
                    )
                articlesRepository.createArticle(userEntity, createArticleDto)
            }

            // When - First page (limit 3, offset 0)
            val firstPage = articlesRepository.getAllArticles(limit = 3, offset = 0)

            // Then
            assertEquals(3, firstPage.size)

            // When - Second page (limit 3, offset 3)
            val secondPage = articlesRepository.getAllArticles(limit = 3, offset = 3)

            // Then
            assertEquals(3, secondPage.size)

            // Ensure first and second page articles are different
            val firstPageIds = firstPage.map { it.id.value }
            val secondPageIds = secondPage.map { it.id.value }
            assertEquals(0, firstPageIds.intersect(secondPageIds).size)

            // When - Last page with fewer items (limit 3, offset 9)
            val lastPage = articlesRepository.getAllArticles(limit = 3, offset = 9)

            // Then
            assertEquals(1, lastPage.size)
        }
}
