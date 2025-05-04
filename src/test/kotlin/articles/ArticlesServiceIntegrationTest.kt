package me.bossm0n5t3r.articles

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
import me.bossm0n5t3r.profiles.FollowingsRepository
import me.bossm0n5t3r.tags.TagsRepository
import me.bossm0n5t3r.users.UsersRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ArticlesServiceIntegrationTest {
    private val databaseManager: DatabaseManager = DatabaseManagerImpl()
    private val usersRepository: UsersRepository = UsersRepository(databaseManager)
    private val followingsRepository: FollowingsRepository = FollowingsRepository(databaseManager)
    private val articlesRepository: ArticlesRepository = ArticlesRepository(databaseManager)
    private val favoriteArticlesRepository: FavoriteArticlesRepository = FavoriteArticlesRepository(databaseManager)
    private val tagsRepository: TagsRepository = TagsRepository(databaseManager)
    private val articlesService: ArticlesService =
        ArticlesService(
            usersRepository,
            followingsRepository,
            articlesRepository,
            favoriteArticlesRepository,
            tagsRepository,
        )

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
    fun testGetAllArticlesWithoutFilters() =
        runBlocking {
            // Given
            val user1Entity = usersRepository.getUserEntityById(user1Id)
            val user2Entity = usersRepository.getUserEntityById(user2Id)

            // Create articles for user1
            val article1 =
                articlesRepository.createArticle(
                    user1Entity,
                    CreateArticleDto(
                        title = "User1 Article 1",
                        description = "Description 1",
                        body = "Body 1",
                        tagList = listOf("tag1", "tag2"),
                    ),
                )

            // Create articles for user2
            val article2 =
                articlesRepository.createArticle(
                    user2Entity,
                    CreateArticleDto(
                        title = "User2 Article 1",
                        description = "Description 2",
                        body = "Body 2",
                        tagList = listOf("tag2", "tag3"),
                    ),
                )

            // Add tags to articles
            val tag1 = tagsRepository.getOrCreateTag("tag1")
            val tag2 = tagsRepository.getOrCreateTag("tag2")
            val tag3 = tagsRepository.getOrCreateTag("tag3")

            tagsRepository.createArticleTagEntity(article1, tag1)
            tagsRepository.createArticleTagEntity(article1, tag2)
            tagsRepository.createArticleTagEntity(article2, tag2)
            tagsRepository.createArticleTagEntity(article2, tag3)

            // Mark article2 as favorited by user1
            favoriteArticlesRepository.favoriteArticle(article2, user1Entity)

            // When
            val result = articlesService.getAllArticles(user1Id, ArticleFilterDto())

            // Then
            assertEquals(2, result.articlesCount)
            assertEquals(2, result.articles.size)

            // Verify article details
            val articleDtos = result.articles.associateBy { it.slug }

            // Check article1
            val articleDto1 = articleDtos[article1.slug]
            assertNotNull(articleDto1)
            assertEquals("User1 Article 1", articleDto1.title)
            assertEquals("Description 1", articleDto1.description)
            assertEquals("Body 1", articleDto1.body)
            assertEquals(2, articleDto1.tagList.size)
            assertTrue(articleDto1.tagList.contains("tag1"))
            assertTrue(articleDto1.tagList.contains("tag2"))
            assertFalse(articleDto1.favorited)
            assertEquals(0, articleDto1.favoritesCount)
            assertEquals(user1Username, articleDto1.author.username)
            assertFalse(articleDto1.author.following)

            // Check article2
            val articleDto2 = articleDtos[article2.slug]
            assertNotNull(articleDto2)
            assertEquals("User2 Article 1", articleDto2.title)
            assertEquals("Description 2", articleDto2.description)
            assertEquals("Body 2", articleDto2.body)
            assertEquals(2, articleDto2.tagList.size)
            assertTrue(articleDto2.tagList.contains("tag2"))
            assertTrue(articleDto2.tagList.contains("tag3"))
            assertTrue(articleDto2.favorited)
            assertEquals(1, articleDto2.favoritesCount)
            assertEquals(user2Username, articleDto2.author.username)
            assertFalse(articleDto2.author.following)
        }

    @Test
    fun testGetAllArticlesWithAuthorFilter() =
        runBlocking {
            // Given
            val user1Entity = usersRepository.getUserEntityById(user1Id)
            val user2Entity = usersRepository.getUserEntityById(user2Id)

            // Create articles for user1
            articlesRepository.createArticle(
                user1Entity,
                CreateArticleDto(
                    title = "User1 Article 1",
                    description = "Description 1",
                    body = "Body 1",
                ),
            )

            articlesRepository.createArticle(
                user1Entity,
                CreateArticleDto(
                    title = "User1 Article 2",
                    description = "Description 2",
                    body = "Body 2",
                ),
            )

            // Create article for user2
            articlesRepository.createArticle(
                user2Entity,
                CreateArticleDto(
                    title = "User2 Article 1",
                    description = "Description 3",
                    body = "Body 3",
                ),
            )

            // When - Filter by user1 as author
            val result =
                articlesService.getAllArticles(
                    user2Id,
                    ArticleFilterDto(author = user1Username),
                )

            // Then
            assertEquals(2, result.articlesCount)
            assertEquals(2, result.articles.size)

            // All articles should be authored by user1
            result.articles.forEach { article ->
                assertEquals(user1Username, article.author.username)
            }
        }

    @Test
    fun testGetAllArticlesWithTagFilter() =
        runBlocking {
            // Given
            val user1Entity = usersRepository.getUserEntityById(user1Id)
            val user2Entity = usersRepository.getUserEntityById(user2Id)

            // Create articles
            val article1 =
                articlesRepository.createArticle(
                    user1Entity,
                    CreateArticleDto(
                        title = "Article with tag1",
                        description = "Description 1",
                        body = "Body 1",
                    ),
                )

            val article2 =
                articlesRepository.createArticle(
                    user2Entity,
                    CreateArticleDto(
                        title = "Article with tag1 and tag2",
                        description = "Description 2",
                        body = "Body 2",
                    ),
                )

            val article3 =
                articlesRepository.createArticle(
                    user1Entity,
                    CreateArticleDto(
                        title = "Article with tag2",
                        description = "Description 3",
                        body = "Body 3",
                    ),
                )

            // Add tags to articles
            val tag1 = tagsRepository.getOrCreateTag("tag1")
            val tag2 = tagsRepository.getOrCreateTag("tag2")

            tagsRepository.createArticleTagEntity(article1, tag1)
            tagsRepository.createArticleTagEntity(article2, tag1)
            tagsRepository.createArticleTagEntity(article2, tag2)
            tagsRepository.createArticleTagEntity(article3, tag2)

            // When - Filter by tag1
            val result =
                articlesService.getAllArticles(
                    user1Id,
                    ArticleFilterDto(tag = "tag1"),
                )

            // Then
            assertEquals(2, result.articlesCount)
            assertEquals(2, result.articles.size)

            // All articles should have tag1
            result.articles.forEach { article ->
                assertTrue(article.tagList.contains("tag1"))
            }
        }

    @Test
    fun testGetAllArticlesWithFavoritedFilter() =
        runBlocking {
            // Given
            val user1Entity = usersRepository.getUserEntityById(user1Id)
            val user2Entity = usersRepository.getUserEntityById(user2Id)

            // Create articles
            val article1 =
                articlesRepository.createArticle(
                    user1Entity,
                    CreateArticleDto(
                        title = "Article 1",
                        description = "Description 1",
                        body = "Body 1",
                    ),
                )

            val article2 =
                articlesRepository.createArticle(
                    user2Entity,
                    CreateArticleDto(
                        title = "Article 2",
                        description = "Description 2",
                        body = "Body 2",
                    ),
                )

            val article3 =
                articlesRepository.createArticle(
                    user1Entity,
                    CreateArticleDto(
                        title = "Article 3",
                        description = "Description 3",
                        body = "Body 3",
                    ),
                )

            // User1 favorites article2
            favoriteArticlesRepository.favoriteArticle(article2, user1Entity)

            // User2 favorites article1 and article3
            favoriteArticlesRepository.favoriteArticle(article1, user2Entity)
            favoriteArticlesRepository.favoriteArticle(article3, user2Entity)

            // When - Get articles favorited by user2
            val result =
                articlesService.getAllArticles(
                    user1Id,
                    ArticleFilterDto(favorited = user2Username),
                )

            // Then
            assertEquals(2, result.articlesCount)
            assertEquals(2, result.articles.size)

            // Verify the correct articles are returned
            val slugs = result.articles.map { it.slug }
            assertTrue(slugs.contains(article1.slug))
            assertTrue(slugs.contains(article3.slug))
        }

    @Test
    fun testGetAllArticlesWithFollowingRelationship() =
        runBlocking {
            // Given
            val user1Entity = usersRepository.getUserEntityById(user1Id)
            val user2Entity = usersRepository.getUserEntityById(user2Id)

            // User1 follows user2
            followingsRepository.addFollowing(user2Id, user1Id)

            // Create articles
            articlesRepository.createArticle(
                user1Entity,
                CreateArticleDto(
                    title = "User1 Article",
                    description = "Description 1",
                    body = "Body 1",
                ),
            )

            articlesRepository.createArticle(
                user2Entity,
                CreateArticleDto(
                    title = "User2 Article",
                    description = "Description 2",
                    body = "Body 2",
                ),
            )

            // When
            val result = articlesService.getAllArticles(user1Id, ArticleFilterDto())

            // Then
            assertEquals(2, result.articlesCount)

            // Find the article by user2
            val user2Article = result.articles.find { it.author.username == user2Username }
            assertNotNull(user2Article)

            // Verify following status is correct
            assertTrue(user2Article.author.following)

            // Find the article by user1
            val user1Article = result.articles.find { it.author.username == user1Username }
            assertNotNull(user1Article)

            // User1 is not following themselves
            assertFalse(user1Article.author.following)
        }

    @Test
    fun testGetAllArticlesWithLimitAndOffset() =
        runBlocking {
            // Given
            val user1Entity = usersRepository.getUserEntityById(user1Id)

            // Create 5 articles
            repeat(5) { index ->
                articlesRepository.createArticle(
                    user1Entity,
                    CreateArticleDto(
                        title = "Article $index",
                        description = "Description $index",
                        body = "Body $index",
                    ),
                )
            }

            // When - Get first 2 articles
            val firstPage =
                articlesService.getAllArticles(
                    user1Id,
                    ArticleFilterDto(limit = 2, offset = 0),
                )

            // Then
            assertEquals(2, firstPage.articles.size)
            assertEquals(2, firstPage.articlesCount)

            // When - Get next 2 articles
            val secondPage =
                articlesService.getAllArticles(
                    user1Id,
                    ArticleFilterDto(limit = 2, offset = 2),
                )

            // Then
            assertEquals(2, secondPage.articles.size)
            assertEquals(2, secondPage.articlesCount)

            // Ensure first and second page articles are different
            val firstPageSlugs = firstPage.articles.map { it.slug }.toSet()
            val secondPageSlugs = secondPage.articles.map { it.slug }.toSet()
            assertEquals(0, firstPageSlugs.intersect(secondPageSlugs).size)
        }
}
