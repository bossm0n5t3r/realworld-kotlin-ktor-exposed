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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FavoriteArticlesRepositoryTest {
    private val databaseManager: DatabaseManager = DatabaseManagerImpl()
    private val favoriteArticlesRepository: FavoriteArticlesRepository = FavoriteArticlesRepository(databaseManager)
    private val articlesRepository: ArticlesRepository = ArticlesRepository(databaseManager)
    private val usersRepository: UsersRepository = UsersRepository(databaseManager)

    private lateinit var user1Entity: UserEntity
    private lateinit var user2Entity: UserEntity
    private lateinit var articleEntity: ArticleEntity

    @BeforeTest
    fun setup() =
        runBlocking {
            databaseManager.createTables() // Create tables before each test

            // Create test users
            user1Entity = usersRepository.findUserEntityByEmail("user1@example.com")
                ?: usersRepository
                    .createUser("user1", "user1@example.com", "password1", "salt1")
                    .let { usersRepository.findUserEntityByEmail("user1@example.com")!! }

            user2Entity = usersRepository.findUserEntityByEmail("user2@example.com")
                ?: usersRepository
                    .createUser("user2", "user2@example.com", "password2", "salt2")
                    .let { usersRepository.findUserEntityByEmail("user2@example.com")!! }

            // Create a test article
            val createArticleDto =
                CreateArticleDto(
                    title = "Test Article",
                    description = "This is a test article",
                    body = "This is the body of the test article",
                )
            articleEntity = articlesRepository.createArticle(user1Entity, createArticleDto)
        }

    @AfterTest
    fun tearDown() = databaseManager.dropTables() // Drop tables after each test

    @Test
    fun testFavoriteArticle() =
        runBlocking {
            // Given
            // user1, user2, and article are created in setup

            // When
            val favoriteArticleEntity = favoriteArticlesRepository.favoriteArticle(articleEntity, user2Entity)

            // Then
            assertNotNull(favoriteArticleEntity)
            assertEquals(articleEntity.id, favoriteArticleEntity.articleId)
            assertEquals(user2Entity.id, favoriteArticleEntity.userId)
        }

    @Test
    fun testUnfavoriteArticle() =
        runBlocking {
            // Given
            // user1, user2, and article are created in setup
            favoriteArticlesRepository.favoriteArticle(articleEntity, user2Entity)
            assertTrue(favoriteArticlesRepository.isFavoritedArticle(articleEntity, user2Entity))

            // When
            favoriteArticlesRepository.unfavoriteArticle(articleEntity, user2Entity)

            // Then
            assertFalse(favoriteArticlesRepository.isFavoritedArticle(articleEntity, user2Entity))
        }

    @Test
    fun testIsFavoritedArticle() =
        runBlocking {
            // Given
            // user1, user2, and article are created in setup

            // When - Check before favoriting
            val isNotFavorited = favoriteArticlesRepository.isFavoritedArticle(articleEntity, user2Entity)

            // Then
            assertFalse(isNotFavorited)

            // When - Favorite the article and check again
            favoriteArticlesRepository.favoriteArticle(articleEntity, user2Entity)
            val isFavorited = favoriteArticlesRepository.isFavoritedArticle(articleEntity, user2Entity)

            // Then
            assertTrue(isFavorited)
        }

    @Test
    fun testGetFavoriteArticleIds() =
        runBlocking {
            // Given
            // user1, user2, and article are created in setup

            // Create another article
            val createArticleDto2 =
                CreateArticleDto(
                    title = "Another Test Article",
                    description = "This is another test article",
                    body = "This is the body of another test article",
                )
            val articleEntity2 = articlesRepository.createArticle(user1Entity, createArticleDto2)

            // User2 favorites both articles
            favoriteArticlesRepository.favoriteArticle(articleEntity, user2Entity)
            favoriteArticlesRepository.favoriteArticle(articleEntity2, user2Entity)

            // When
            val favoriteArticleIds = favoriteArticlesRepository.getFavoriteArticleIds(user2Entity)

            // Then
            assertEquals(2, favoriteArticleIds.size)
            assertTrue(favoriteArticleIds.contains(articleEntity.id.value.toString()))
            assertTrue(favoriteArticleIds.contains(articleEntity2.id.value.toString()))
        }

    @Test
    fun testGetFavoritesCount() =
        runBlocking {
            // Given
            // user1, user2, and article are created in setup

            // Initially, no users have favorited the article
            val initialCount = favoriteArticlesRepository.getFavoritesCount(articleEntity)
            assertEquals(0, initialCount)

            // When - User2 favorites the article
            favoriteArticlesRepository.favoriteArticle(articleEntity, user2Entity)

            // Then - Count should be 1
            val countAfterOneFavorite = favoriteArticlesRepository.getFavoritesCount(articleEntity)
            assertEquals(1, countAfterOneFavorite)

            // When - Create another user and have them favorite the article
            val user3Entity =
                usersRepository.findUserEntityByEmail("user3@example.com")
                    ?: usersRepository
                        .createUser("user3", "user3@example.com", "password3", "salt3")
                        .let { usersRepository.findUserEntityByEmail("user3@example.com")!! }
            favoriteArticlesRepository.favoriteArticle(articleEntity, user3Entity)

            // Then - Count should be 2
            val countAfterTwoFavorites = favoriteArticlesRepository.getFavoritesCount(articleEntity)
            assertEquals(2, countAfterTwoFavorites)

            // When - User2 unfavorites the article
            favoriteArticlesRepository.unfavoriteArticle(articleEntity, user2Entity)

            // Then - Count should be 1 again
            val countAfterUnfavorite = favoriteArticlesRepository.getFavoritesCount(articleEntity)
            assertEquals(1, countAfterUnfavorite)
        }
}
