package me.bossm0n5t3r.articles

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.profiles.FollowingsRepository
import me.bossm0n5t3r.tags.TagEntity
import me.bossm0n5t3r.tags.TagsRepository
import me.bossm0n5t3r.users.UserEntity
import me.bossm0n5t3r.users.Users
import me.bossm0n5t3r.users.UsersRepository
import org.jetbrains.exposed.dao.id.EntityID
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArticlesServiceMockTest {
    private val usersRepository: UsersRepository = mockk(relaxed = true)
    private val followingsRepository: FollowingsRepository = mockk(relaxed = true)
    private val articlesRepository: ArticlesRepository = mockk(relaxed = true)
    private val favoriteArticlesRepository: FavoriteArticlesRepository = mockk(relaxed = true)
    private val tagsRepository: TagsRepository = mockk(relaxed = true)
    private val articlesService: ArticlesService =
        ArticlesService(
            usersRepository,
            followingsRepository,
            articlesRepository,
            favoriteArticlesRepository,
            tagsRepository,
        )

    @Test
    fun testGetAllArticlesWithoutFilters() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val articleFilterDto = ArticleFilterDto()

            val articleId = UUID.randomUUID()
            val authorId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()
            val now = Instant.now()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns "test-article"
            every { articleEntity.title } returns "Test Article"
            every { articleEntity.description } returns "Test Description"
            every { articleEntity.body } returns "Test Body"
            every { articleEntity.authorId } returns EntityID(authorId, Users)
            every { articleEntity.createdAt } returns now
            every { articleEntity.updatedAt } returns now

            // Set up the author entity
            val authorEntity = mockk<UserEntity>()
            every { authorEntity.id } returns EntityID(authorId, Users)
            every { authorEntity.username } returns "testauthor"
            every { authorEntity.bio } returns "Author Bio"
            every { authorEntity.image } returns "https://example.com/author.jpg"

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getAllArticles(null, 20, 0) } returns listOf(articleEntity)
            coEvery { usersRepository.getUserEntityById(EntityID(authorId, Users)) } returns authorEntity
            coEvery { tagsRepository.getAllTagsByArticle(articleEntity) } returns emptyList()
            coEvery { favoriteArticlesRepository.getFavoritesCount(articleEntity) } returns 0
            coEvery { favoriteArticlesRepository.isFavoritedArticle(articleEntity, userEntity) } returns false
            coEvery { followingsRepository.isFollowing(authorId.toString(), userId) } returns false

            // When
            val result = articlesService.getAllArticles(userId, articleFilterDto)

            // Then
            assertEquals(1, result.articlesCount)
            assertEquals(1, result.articles.size)

            val articleDto = result.articles[0]
            assertEquals("test-article", articleDto.slug)
            assertEquals("Test Article", articleDto.title)
            assertEquals("Test Description", articleDto.description)
            assertEquals("Test Body", articleDto.body)
            assertEquals(emptyList(), articleDto.tagList)
            assertEquals(now.toString(), articleDto.createdAt)
            assertEquals(now.toString(), articleDto.updatedAt)
            assertFalse(articleDto.favorited)
            assertEquals(0, articleDto.favoritesCount)
            assertEquals("testauthor", articleDto.author.username)
            assertEquals("Author Bio", articleDto.author.bio)
            assertEquals("https://example.com/author.jpg", articleDto.author.image)
            assertFalse(articleDto.author.following)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getAllArticles(null, 20, 0) }
            coVerify { usersRepository.getUserEntityById(EntityID(authorId, Users)) }
            coVerify { tagsRepository.getAllTagsByArticle(articleEntity) }
            coVerify { favoriteArticlesRepository.getFavoritesCount(articleEntity) }
            coVerify { favoriteArticlesRepository.isFavoritedArticle(articleEntity, userEntity) }
            coVerify { followingsRepository.isFollowing(authorId.toString(), userId) }
        }

    @Test
    fun testGetAllArticlesWithAuthorFilter() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val authorUsername = "filterauthor"
            val articleFilterDto = ArticleFilterDto(author = authorUsername)

            val articleId = UUID.randomUUID()
            val authorId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()
            val now = Instant.now()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the author entity for filter
            val authorEntity = mockk<UserEntity>()
            every { authorEntity.id } returns EntityID(authorId, Users)
            every { authorEntity.username } returns authorUsername
            every { authorEntity.bio } returns "Filter Author Bio"
            every { authorEntity.image } returns "https://example.com/filterauthor.jpg"

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns "author-article"
            every { articleEntity.title } returns "Author Article"
            every { articleEntity.description } returns "Author Description"
            every { articleEntity.body } returns "Author Body"
            every { articleEntity.authorId } returns EntityID(authorId, Users)
            every { articleEntity.createdAt } returns now
            every { articleEntity.updatedAt } returns now

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { usersRepository.findUserEntityByUsername(authorUsername) } returns authorEntity
            coEvery { articlesRepository.getAllArticles(authorEntity, 20, 0) } returns listOf(articleEntity)
            coEvery { usersRepository.getUserEntityById(EntityID(authorId, Users)) } returns authorEntity
            coEvery { tagsRepository.getAllTagsByArticle(articleEntity) } returns emptyList()
            coEvery { favoriteArticlesRepository.getFavoritesCount(articleEntity) } returns 0
            coEvery { favoriteArticlesRepository.isFavoritedArticle(articleEntity, userEntity) } returns false
            coEvery { followingsRepository.isFollowing(authorId.toString(), userId) } returns true

            // When
            val result = articlesService.getAllArticles(userId, articleFilterDto)

            // Then
            assertEquals(1, result.articlesCount)
            assertEquals(1, result.articles.size)

            val articleDto = result.articles[0]
            assertEquals("author-article", articleDto.slug)
            assertEquals("Author Article", articleDto.title)
            assertEquals("Author Description", articleDto.description)
            assertEquals("Author Body", articleDto.body)
            assertEquals(emptyList(), articleDto.tagList)
            assertEquals(now.toString(), articleDto.createdAt)
            assertEquals(now.toString(), articleDto.updatedAt)
            assertFalse(articleDto.favorited)
            assertEquals(0, articleDto.favoritesCount)
            assertEquals(authorUsername, articleDto.author.username)
            assertTrue(articleDto.author.following)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { usersRepository.findUserEntityByUsername(authorUsername) }
            coVerify { articlesRepository.getAllArticles(authorEntity, 20, 0) }
        }

    @Test
    fun testGetAllArticlesWithTagFilter() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val tagName = "testtag"
            val articleFilterDto = ArticleFilterDto(tag = tagName)

            val articleId = UUID.randomUUID()
            val authorId = UUID.randomUUID()
            val tagId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()
            val now = Instant.now()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the tag entity
            val tagEntity = mockk<TagEntity>()
            every { tagEntity.id } returns EntityID(tagId, me.bossm0n5t3r.tags.Tags)
            every { tagEntity.tagName } returns tagName

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns "tagged-article"
            every { articleEntity.title } returns "Tagged Article"
            every { articleEntity.description } returns "Tagged Description"
            every { articleEntity.body } returns "Tagged Body"
            every { articleEntity.authorId } returns EntityID(authorId, Users)
            every { articleEntity.createdAt } returns now
            every { articleEntity.updatedAt } returns now

            // Set up the author entity
            val authorEntity = mockk<UserEntity>()
            every { authorEntity.id } returns EntityID(authorId, Users)
            every { authorEntity.username } returns "taggedauthor"
            every { authorEntity.bio } returns "Tagged Author Bio"
            every { authorEntity.image } returns "https://example.com/taggedauthor.jpg"

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { tagsRepository.getTagOrNull(tagName) } returns tagEntity
            coEvery { articlesRepository.getAllArticles(null, 20, 0) } returns listOf(articleEntity)
            coEvery { tagsRepository.isArticleHasTag(articleEntity, tagEntity) } returns true
            coEvery { usersRepository.getUserEntityById(EntityID(authorId, Users)) } returns authorEntity
            coEvery { tagsRepository.getAllTagsByArticle(articleEntity) } returns listOf(tagEntity)
            coEvery { favoriteArticlesRepository.getFavoritesCount(articleEntity) } returns 5
            coEvery { favoriteArticlesRepository.isFavoritedArticle(articleEntity, userEntity) } returns true
            coEvery { followingsRepository.isFollowing(authorId.toString(), userId) } returns false

            // When
            val result = articlesService.getAllArticles(userId, articleFilterDto)

            // Then
            assertEquals(1, result.articlesCount)
            assertEquals(1, result.articles.size)

            val articleDto = result.articles[0]
            assertEquals("tagged-article", articleDto.slug)
            assertEquals("Tagged Article", articleDto.title)
            assertEquals("Tagged Description", articleDto.description)
            assertEquals("Tagged Body", articleDto.body)
            assertEquals(listOf(tagName), articleDto.tagList)
            assertEquals(now.toString(), articleDto.createdAt)
            assertEquals(now.toString(), articleDto.updatedAt)
            assertTrue(articleDto.favorited)
            assertEquals(5, articleDto.favoritesCount)
            assertEquals("taggedauthor", articleDto.author.username)
            assertEquals("Tagged Author Bio", articleDto.author.bio)
            assertEquals("https://example.com/taggedauthor.jpg", articleDto.author.image)
            assertFalse(articleDto.author.following)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { tagsRepository.getTagOrNull(tagName) }
            coVerify { articlesRepository.getAllArticles(null, 20, 0) }
            coVerify { tagsRepository.isArticleHasTag(articleEntity, tagEntity) }
        }

    @Test
    fun testGetAllArticlesWithFavoritedFilter() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val favoritedUsername = "favoriteduser"
            val articleFilterDto = ArticleFilterDto(favorited = favoritedUsername)

            val articleId = UUID.randomUUID()
            val authorId = UUID.randomUUID()
            val favoritedUserId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()
            val now = Instant.now()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the favorited user entity
            val favoritedUserEntity = mockk<UserEntity>()
            every { favoritedUserEntity.id } returns EntityID(favoritedUserId, Users)
            every { favoritedUserEntity.username } returns favoritedUsername

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns "favorited-article"
            every { articleEntity.title } returns "Favorited Article"
            every { articleEntity.description } returns "Favorited Description"
            every { articleEntity.body } returns "Favorited Body"
            every { articleEntity.authorId } returns EntityID(authorId, Users)
            every { articleEntity.createdAt } returns now
            every { articleEntity.updatedAt } returns now

            // Set up the author entity
            val authorEntity = mockk<UserEntity>()
            every { authorEntity.id } returns EntityID(authorId, Users)
            every { authorEntity.username } returns "favoritedauthor"
            every { authorEntity.bio } returns "Favorited Author Bio"
            every { authorEntity.image } returns "https://example.com/favoritedauthor.jpg"

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { usersRepository.findUserEntityByUsername(favoritedUsername) } returns favoritedUserEntity
            coEvery { articlesRepository.getAllArticles(null, 20, 0) } returns listOf(articleEntity)
            coEvery { favoriteArticlesRepository.isFavoritedArticle(articleEntity, favoritedUserEntity) } returns true
            coEvery { usersRepository.getUserEntityById(EntityID(authorId, Users)) } returns authorEntity
            coEvery { tagsRepository.getAllTagsByArticle(articleEntity) } returns emptyList()
            coEvery { favoriteArticlesRepository.getFavoritesCount(articleEntity) } returns 10
            coEvery { favoriteArticlesRepository.isFavoritedArticle(articleEntity, userEntity) } returns false
            coEvery { followingsRepository.isFollowing(authorId.toString(), userId) } returns true

            // When
            val result = articlesService.getAllArticles(userId, articleFilterDto)

            // Then
            assertEquals(1, result.articlesCount)
            assertEquals(1, result.articles.size)

            val articleDto = result.articles[0]
            assertEquals("favorited-article", articleDto.slug)
            assertEquals("Favorited Article", articleDto.title)
            assertEquals("Favorited Description", articleDto.description)
            assertEquals("Favorited Body", articleDto.body)
            assertEquals(emptyList(), articleDto.tagList)
            assertEquals(now.toString(), articleDto.createdAt)
            assertEquals(now.toString(), articleDto.updatedAt)
            assertFalse(articleDto.favorited)
            assertEquals(10, articleDto.favoritesCount)
            assertEquals("favoritedauthor", articleDto.author.username)
            assertEquals("Favorited Author Bio", articleDto.author.bio)
            assertEquals("https://example.com/favoritedauthor.jpg", articleDto.author.image)
            assertTrue(articleDto.author.following)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { usersRepository.findUserEntityByUsername(favoritedUsername) }
            coVerify { articlesRepository.getAllArticles(null, 20, 0) }
            coVerify { favoriteArticlesRepository.isFavoritedArticle(articleEntity, favoritedUserEntity) }
        }
}
