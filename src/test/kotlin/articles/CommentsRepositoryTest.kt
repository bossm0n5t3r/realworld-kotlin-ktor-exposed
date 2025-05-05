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
import kotlin.test.assertTrue

class CommentsRepositoryTest {
    private val databaseManager: DatabaseManager = DatabaseManagerImpl()
    private val commentsRepository: CommentsRepository = CommentsRepository(databaseManager)
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
    fun testCreateComment() =
        runBlocking {
            // Given
            val commentBody = "This is a test comment"

            // When
            val commentEntity = commentsRepository.createComment(userEntity, articleEntity, commentBody)

            // Then
            assertNotNull(commentEntity)
            assertEquals(commentBody, commentEntity.body)
            assertEquals(userEntity.id, commentEntity.userId)
            assertEquals(articleEntity.id, commentEntity.articleId)
        }

    @Test
    fun testGetCommentsForArticle() =
        runBlocking {
            // Given
            val commentBodies = listOf("First comment", "Second comment", "Third comment")

            // Create multiple comments for the article
            commentBodies.forEach { body ->
                commentsRepository.createComment(userEntity, articleEntity, body)
            }

            // When
            val comments = commentsRepository.getCommentsForArticle(articleEntity)

            // Then
            assertEquals(commentBodies.size, comments.size)
            commentBodies.forEach { body ->
                assertTrue(comments.any { it.body == body })
            }
            comments.forEach { comment ->
                assertEquals(articleEntity.id, comment.articleId)
            }
        }

    @Test
    fun testDeleteComment() =
        runBlocking {
            // Given
            val commentBody = "Comment to be deleted"
            val commentEntity = commentsRepository.createComment(userEntity, articleEntity, commentBody)

            // Verify comment exists before deletion
            val commentsBeforeDeletion = commentsRepository.getCommentsForArticle(articleEntity)
            assertTrue(commentsBeforeDeletion.any { it.id == commentEntity.id })

            // When
            commentsRepository.deleteComment(commentEntity)

            // Then
            val commentsAfterDeletion = commentsRepository.getCommentsForArticle(articleEntity)
            assertTrue(commentsAfterDeletion.none { it.id == commentEntity.id })
        }

    @Test
    fun testGetCommentsForArticle_NoComments() =
        runBlocking {
            // Given
            // No comments created for the article

            // When
            val comments = commentsRepository.getCommentsForArticle(articleEntity)

            // Then
            assertTrue(comments.isEmpty())
        }

    @Test
    fun testGetCommentsForArticle_MultipleUsers() =
        runBlocking {
            // Given
            // Create another user
            val anotherUserEntity =
                usersRepository.findUserEntityByEmail("another@example.com")
                    ?: usersRepository
                        .createUser("another", "another@example.com", "password", "salt")
                        .let { usersRepository.findUserEntityByEmail("another@example.com")!! }

            // Create comments from different users
            val comment1 = commentsRepository.createComment(userEntity, articleEntity, "Comment from user 1")
            val comment2 = commentsRepository.createComment(anotherUserEntity, articleEntity, "Comment from user 2")

            // When
            val comments = commentsRepository.getCommentsForArticle(articleEntity)

            // Then
            assertEquals(2, comments.size)
            assertTrue(comments.any { it.id == comment1.id && it.userId == userEntity.id })
            assertTrue(comments.any { it.id == comment2.id && it.userId == anotherUserEntity.id })
        }
}
