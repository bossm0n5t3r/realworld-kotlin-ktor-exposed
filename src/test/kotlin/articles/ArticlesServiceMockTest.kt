package me.bossm0n5t3r.articles

import io.mockk.coEvery
import io.mockk.coJustRun
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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArticlesServiceMockTest {
    private val usersRepository: UsersRepository = mockk(relaxed = true)
    private val followingsRepository: FollowingsRepository = mockk(relaxed = true)
    private val articlesRepository: ArticlesRepository = mockk(relaxed = true)
    private val favoriteArticlesRepository: FavoriteArticlesRepository = mockk(relaxed = true)
    private val tagsRepository: TagsRepository = mockk(relaxed = true)
    private val commentsRepository: CommentsRepository = mockk(relaxed = true)
    private val articlesService: ArticlesService =
        ArticlesService(
            usersRepository,
            followingsRepository,
            articlesRepository,
            favoriteArticlesRepository,
            tagsRepository,
            commentsRepository,
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

    @Test
    fun testGetArticleBySlug() =
        runBlocking {
            // Given
            val slug = "test-article-slug"
            val articleId = UUID.randomUUID()
            val authorId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()
            val now = Instant.now()

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug
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
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coEvery { usersRepository.getUserEntityById(EntityID(authorId, Users)) } returns authorEntity
            coEvery { tagsRepository.getAllTagsByArticle(articleEntity) } returns emptyList()
            coEvery { favoriteArticlesRepository.getFavoritesCount(articleEntity) } returns 0
            // No need to mock isFavoritedArticle with null as it's handled by the Elvis operator in the service
            coEvery { followingsRepository.isFollowing(any(), any()) } returns false

            // When
            val result = articlesService.getArticleBySlug(slug)

            // Then
            assertEquals(slug, result.article.slug)
            assertEquals("Test Article", result.article.title)
            assertEquals("Test Description", result.article.description)
            assertEquals("Test Body", result.article.body)
            assertEquals(emptyList(), result.article.tagList)
            assertEquals(now.toString(), result.article.createdAt)
            assertEquals(now.toString(), result.article.updatedAt)
            assertFalse(result.article.favorited)
            assertEquals(0, result.article.favoritesCount)
            assertEquals("testauthor", result.article.author.username)
            assertEquals("Author Bio", result.article.author.bio)
            assertEquals("https://example.com/author.jpg", result.article.author.image)
            assertFalse(result.article.author.following)

            // Verify repository calls
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { usersRepository.getUserEntityById(EntityID(authorId, Users)) }
            coVerify { tagsRepository.getAllTagsByArticle(articleEntity) }
            coVerify { favoriteArticlesRepository.getFavoritesCount(articleEntity) }
        }

    @Test
    fun testGetArticleBySlugNotFound() =
        runBlocking {
            // Given
            val slug = "non-existent-article"

            // Mock repository methods
            coEvery { articlesRepository.getArticleBySlug(slug) } returns null

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.getArticleBySlug(slug)
            }

            // Verify repository calls
            coVerify { articlesRepository.getArticleBySlug(slug) }
        }

    @Test
    fun testCreateArticle() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val createArticleDto =
                CreateArticleDto(
                    title = "New Article",
                    description = "New Description",
                    body = "New Body",
                    tagList = listOf("tag1", "tag2"),
                )

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()
            val now = Instant.now()

            // Set up the user entity properties
            val userUUID = UUID.fromString(userId)
            val userEntityId = mockk<EntityID<UUID>>()
            every { userEntity.id } returns userEntityId
            every { userEntityId.value } returns userUUID
            every { userEntity.username } returns "testuser"
            every { userEntity.bio } returns "Test Bio"
            every { userEntity.image } returns "https://example.com/testuser.jpg"

            // Set up the article entity properties
            val articleEntityId = mockk<EntityID<UUID>>()
            every { articleEntity.id } returns articleEntityId
            every { articleEntityId.value } returns articleId
            every { articleEntity.slug } returns "new-article"
            every { articleEntity.title } returns createArticleDto.title
            every { articleEntity.description } returns createArticleDto.description
            every { articleEntity.body } returns createArticleDto.body
            // Make sure the article's authorId equals the user's id for the getArticleDto method
            every { articleEntity.authorId } returns userEntityId
            every { articleEntity.createdAt } returns now
            every { articleEntity.updatedAt } returns now

            // Set up tag entities
            val tag1Entity = mockk<TagEntity>()
            val tag2Entity = mockk<TagEntity>()
            every { tag1Entity.tagName } returns "tag1"
            every { tag2Entity.tagName } returns "tag2"

            // Set up the author entity (same as user in this case)
            val authorEntity = userEntity

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { usersRepository.getUserEntityById(userEntityId) } returns authorEntity
            coEvery { articlesRepository.createArticle(userEntity, createArticleDto) } returns articleEntity
            coEvery { tagsRepository.getOrCreateTag("tag1") } returns tag1Entity
            coEvery { tagsRepository.getOrCreateTag("tag2") } returns tag2Entity
            coEvery { tagsRepository.createArticleTagEntity(articleEntity, any()) } returns mockk()
            coEvery { tagsRepository.getAllTagsByArticle(articleEntity) } returns listOf(tag1Entity, tag2Entity)
            coEvery { favoriteArticlesRepository.getFavoritesCount(articleEntity) } returns 0
            coEvery { favoriteArticlesRepository.isFavoritedArticle(articleEntity, userEntity) } returns false
            coEvery { followingsRepository.isFollowing(userUUID.toString(), userUUID.toString()) } returns false

            // When
            val result = articlesService.createArticle(userId, createArticleDto)

            // Then
            assertEquals("new-article", result.article.slug)
            assertEquals(createArticleDto.title, result.article.title)
            assertEquals(createArticleDto.description, result.article.description)
            assertEquals(createArticleDto.body, result.article.body)
            assertEquals(createArticleDto.tagList, result.article.tagList)
            assertEquals(now.toString(), result.article.createdAt)
            assertEquals(now.toString(), result.article.updatedAt)
            assertFalse(result.article.favorited)
            assertEquals(0, result.article.favoritesCount)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.createArticle(userEntity, createArticleDto) }
            coVerify { tagsRepository.getOrCreateTag("tag1") }
            coVerify { tagsRepository.getOrCreateTag("tag2") }
            coVerify { tagsRepository.createArticleTagEntity(articleEntity, tag1Entity) }
            coVerify { tagsRepository.createArticleTagEntity(articleEntity, tag2Entity) }
        }

    @Test
    fun testUpdateArticle() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "existing-article"
            val updateArticleDto =
                UpdateArticleDto(
                    title = "Updated Title",
                    description = "Updated Description",
                    body = "Updated Body",
                )

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()
            val updatedArticleEntity = mockk<ArticleEntity>()
            val now = Instant.now()

            // Set up the user entity properties
            val userUUID = UUID.fromString(userId)
            val userEntityId = mockk<EntityID<UUID>>()
            every { userEntity.id } returns userEntityId
            every { userEntityId.value } returns userUUID
            every { userEntity.username } returns "testuser"
            every { userEntity.bio } returns "Test Bio"
            every { userEntity.image } returns "https://example.com/testuser.jpg"

            // Set up the article entity properties
            val articleEntityId = mockk<EntityID<UUID>>()
            every { articleEntity.id } returns articleEntityId
            every { articleEntityId.value } returns articleId
            every { articleEntity.slug } returns slug
            // Make sure the article's authorId equals the user's id for the authorization check
            every { articleEntity.authorId } returns userEntityId

            // Set up the updated article entity properties
            val updatedArticleEntityId = mockk<EntityID<UUID>>()
            every { updatedArticleEntity.id } returns updatedArticleEntityId
            every { updatedArticleEntityId.value } returns articleId
            every { updatedArticleEntity.slug } returns slug
            every { updatedArticleEntity.title } returns updateArticleDto.title!!
            every { updatedArticleEntity.description } returns updateArticleDto.description!!
            every { updatedArticleEntity.body } returns updateArticleDto.body!!
            // Make sure the updated article's authorId equals the user's id
            every { updatedArticleEntity.authorId } returns userEntityId
            every { updatedArticleEntity.createdAt } returns now
            every { updatedArticleEntity.updatedAt } returns now

            // Set up the author entity (same as user in this case)
            val authorEntity = userEntity

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { usersRepository.getUserEntityById(userEntityId) } returns authorEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coEvery { articlesRepository.updateArticle(articleEntity, updateArticleDto) } returns updatedArticleEntity
            coEvery { tagsRepository.getAllTagsByArticle(updatedArticleEntity) } returns emptyList()
            coEvery { favoriteArticlesRepository.getFavoritesCount(updatedArticleEntity) } returns 0
            coEvery { favoriteArticlesRepository.isFavoritedArticle(updatedArticleEntity, userEntity) } returns false
            coEvery { followingsRepository.isFollowing(userUUID.toString(), userUUID.toString()) } returns false

            // When
            val result = articlesService.updateArticle(userId, slug, updateArticleDto)

            // Then
            assertEquals(slug, result.article.slug)
            assertEquals(updateArticleDto.title, result.article.title)
            assertEquals(updateArticleDto.description, result.article.description)
            assertEquals(updateArticleDto.body, result.article.body)
            assertEquals(emptyList(), result.article.tagList)
            assertEquals(now.toString(), result.article.createdAt)
            assertEquals(now.toString(), result.article.updatedAt)
            assertFalse(result.article.favorited)
            assertEquals(0, result.article.favoritesCount)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { articlesRepository.updateArticle(articleEntity, updateArticleDto) }
        }

    @Test
    fun testUpdateArticleNotFound() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "non-existent-article"
            val updateArticleDto =
                UpdateArticleDto(
                    title = "Updated Title",
                )

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns null

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.updateArticle(userId, slug, updateArticleDto)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
        }

    @Test
    fun testUpdateArticleNotAuthor() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val authorId = UUID.randomUUID()
            val userEntity = mockk<UserEntity>()
            val slug = "other-author-article"
            val updateArticleDto =
                UpdateArticleDto(
                    title = "Updated Title",
                )

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug
            every { articleEntity.authorId } returns EntityID(authorId, Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.updateArticle(userId, slug, updateArticleDto)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
        }

    @Test
    fun testDeleteArticle() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "article-to-delete"

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug
            every { articleEntity.authorId } returns EntityID(UUID.fromString(userId), Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coJustRun { articlesRepository.deleteArticle(articleEntity) }

            // When
            articlesService.deleteArticle(userId, slug)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { articlesRepository.deleteArticle(articleEntity) }
        }

    @Test
    fun testDeleteArticleNotFound() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "non-existent-article"

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns null

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.deleteArticle(userId, slug)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
        }

    @Test
    fun testDeleteArticleNotAuthor() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val authorId = UUID.randomUUID()
            val userEntity = mockk<UserEntity>()
            val slug = "other-author-article"

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug
            every { articleEntity.authorId } returns EntityID(authorId, Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.deleteArticle(userId, slug)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
        }

    @Test
    fun testFavoriteArticle() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "article-to-favorite"

            val articleId = UUID.randomUUID()
            val authorId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()
            val now = Instant.now()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug
            every { articleEntity.title } returns "Article Title"
            every { articleEntity.description } returns "Article Description"
            every { articleEntity.body } returns "Article Body"
            every { articleEntity.authorId } returns EntityID(authorId, Users)
            every { articleEntity.createdAt } returns now
            every { articleEntity.updatedAt } returns now

            // Set up the author entity
            val authorEntity = mockk<UserEntity>()
            every { authorEntity.id } returns EntityID(authorId, Users)
            every { authorEntity.username } returns "authorname"
            every { authorEntity.bio } returns "Author Bio"
            every { authorEntity.image } returns "https://example.com/author.jpg"

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coJustRun { favoriteArticlesRepository.favoriteArticle(articleEntity, userEntity) }
            coEvery { usersRepository.getUserEntityById(EntityID(authorId, Users)) } returns authorEntity
            coEvery { tagsRepository.getAllTagsByArticle(articleEntity) } returns emptyList()
            coEvery { favoriteArticlesRepository.getFavoritesCount(articleEntity) } returns 1
            coEvery { favoriteArticlesRepository.isFavoritedArticle(articleEntity, userEntity) } returns true
            coEvery { followingsRepository.isFollowing(authorId.toString(), userId) } returns false

            // When
            val result = articlesService.favoriteArticle(userId, slug)

            // Then
            assertEquals(slug, result.article.slug)
            assertEquals("Article Title", result.article.title)
            assertEquals("Article Description", result.article.description)
            assertEquals("Article Body", result.article.body)
            assertEquals(emptyList(), result.article.tagList)
            assertEquals(now.toString(), result.article.createdAt)
            assertEquals(now.toString(), result.article.updatedAt)
            assertTrue(result.article.favorited)
            assertEquals(1, result.article.favoritesCount)
            assertEquals("authorname", result.article.author.username)
            assertEquals("Author Bio", result.article.author.bio)
            assertEquals("https://example.com/author.jpg", result.article.author.image)
            assertFalse(result.article.author.following)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { favoriteArticlesRepository.favoriteArticle(articleEntity, userEntity) }
        }

    @Test
    fun testFavoriteArticleNotFound() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "non-existent-article"

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns null

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.favoriteArticle(userId, slug)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
        }

    @Test
    fun testUnfavoriteArticle() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "article-to-unfavorite"

            val articleId = UUID.randomUUID()
            val authorId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()
            val now = Instant.now()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug
            every { articleEntity.title } returns "Article Title"
            every { articleEntity.description } returns "Article Description"
            every { articleEntity.body } returns "Article Body"
            every { articleEntity.authorId } returns EntityID(authorId, Users)
            every { articleEntity.createdAt } returns now
            every { articleEntity.updatedAt } returns now

            // Set up the author entity
            val authorEntity = mockk<UserEntity>()
            every { authorEntity.id } returns EntityID(authorId, Users)
            every { authorEntity.username } returns "authorname"
            every { authorEntity.bio } returns "Author Bio"
            every { authorEntity.image } returns "https://example.com/author.jpg"

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coJustRun { favoriteArticlesRepository.unfavoriteArticle(articleEntity, userEntity) }
            coEvery { usersRepository.getUserEntityById(EntityID(authorId, Users)) } returns authorEntity
            coEvery { tagsRepository.getAllTagsByArticle(articleEntity) } returns emptyList()
            coEvery { favoriteArticlesRepository.getFavoritesCount(articleEntity) } returns 0
            coEvery { favoriteArticlesRepository.isFavoritedArticle(articleEntity, userEntity) } returns false
            coEvery { followingsRepository.isFollowing(authorId.toString(), userId) } returns false

            // When
            val result = articlesService.unfavoriteArticle(userId, slug)

            // Then
            assertEquals(slug, result.article.slug)
            assertEquals("Article Title", result.article.title)
            assertEquals("Article Description", result.article.description)
            assertEquals("Article Body", result.article.body)
            assertEquals(emptyList(), result.article.tagList)
            assertEquals(now.toString(), result.article.createdAt)
            assertEquals(now.toString(), result.article.updatedAt)
            assertFalse(result.article.favorited)
            assertEquals(0, result.article.favoritesCount)
            assertEquals("authorname", result.article.author.username)
            assertEquals("Author Bio", result.article.author.bio)
            assertEquals("https://example.com/author.jpg", result.article.author.image)
            assertFalse(result.article.author.following)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { favoriteArticlesRepository.unfavoriteArticle(articleEntity, userEntity) }
        }

    @Test
    fun testUnfavoriteArticleNotFound() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "non-existent-article"

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns null

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.unfavoriteArticle(userId, slug)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
        }

    @Test
    fun testAddComment() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "test-article"
            val createCommentDto = CreateCommentDto(body = "Test comment body")

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()
            val commentId = 1L
            val commentEntity = mockk<CommentEntity>()
            val now = Instant.now()

            // Set up the user entity properties
            val userUUID = UUID.fromString(userId)
            every { userEntity.id } returns EntityID(userUUID, Users)
            every { userEntity.username } returns "testuser"
            every { userEntity.bio } returns "Test Bio"
            every { userEntity.image } returns "https://example.com/testuser.jpg"

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug

            // Set up the comment entity properties
            every { commentEntity.id } returns EntityID(commentId, Comments)
            every { commentEntity.createdAt } returns now
            every { commentEntity.updatedAt } returns now
            every { commentEntity.body } returns createCommentDto.body
            every { commentEntity.userId } returns EntityID(userUUID, Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { usersRepository.getUserEntityById(EntityID(userUUID, Users)) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coEvery { commentsRepository.createComment(userEntity, articleEntity, createCommentDto.body) } returns commentEntity
            coEvery { followingsRepository.isFollowing(userUUID.toString(), userUUID.toString()) } returns false

            // When
            val result = articlesService.addComment(userId, slug, createCommentDto)

            // Then
            assertEquals(commentId, result.comment.id)
            assertEquals(now.toString(), result.comment.createdAt)
            assertEquals(now.toString(), result.comment.updatedAt)
            assertEquals(createCommentDto.body, result.comment.body)
            assertEquals("testuser", result.comment.author.username)
            assertEquals("Test Bio", result.comment.author.bio)
            assertEquals("https://example.com/testuser.jpg", result.comment.author.image)
            assertFalse(result.comment.author.following)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { commentsRepository.createComment(userEntity, articleEntity, createCommentDto.body) }
        }

    @Test
    fun testAddCommentArticleNotFound() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "non-existent-article"
            val createCommentDto = CreateCommentDto(body = "Test comment body")

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns null

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.addComment(userId, slug, createCommentDto)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
        }

    @Test
    fun testGetCommentsForArticle() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "test-article"

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()

            val commentId1 = 1L
            val commentId2 = 2L
            val commentEntity1 = mockk<CommentEntity>()
            val commentEntity2 = mockk<CommentEntity>()
            val now = Instant.now()

            val commentAuthorId1 = UUID.randomUUID()
            val commentAuthorId2 = UUID.randomUUID()
            val commentAuthorEntity1 = mockk<UserEntity>()
            val commentAuthorEntity2 = mockk<UserEntity>()

            // Set up the user entity properties
            val userUUID = UUID.fromString(userId)
            val userEntityId = mockk<EntityID<UUID>>()
            every { userEntity.id } returns userEntityId
            every { userEntityId.value } returns userUUID

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug

            // Set up the comment entities properties
            every { commentEntity1.id } returns EntityID(commentId1, Comments)
            every { commentEntity1.createdAt } returns now
            every { commentEntity1.updatedAt } returns now
            every { commentEntity1.body } returns "Comment 1 body"
            every { commentEntity1.userId } returns EntityID(commentAuthorId1, Users)

            every { commentEntity2.id } returns EntityID(commentId2, Comments)
            every { commentEntity2.createdAt } returns now
            every { commentEntity2.updatedAt } returns now
            every { commentEntity2.body } returns "Comment 2 body"
            every { commentEntity2.userId } returns EntityID(commentAuthorId2, Users)

            // Set up the comment author entities
            every { commentAuthorEntity1.id } returns EntityID(commentAuthorId1, Users)
            every { commentAuthorEntity1.username } returns "commentauthor1"
            every { commentAuthorEntity1.bio } returns "Comment Author 1 Bio"
            every { commentAuthorEntity1.image } returns "https://example.com/commentauthor1.jpg"

            every { commentAuthorEntity2.id } returns EntityID(commentAuthorId2, Users)
            every { commentAuthorEntity2.username } returns "commentauthor2"
            every { commentAuthorEntity2.bio } returns "Comment Author 2 Bio"
            every { commentAuthorEntity2.image } returns "https://example.com/commentauthor2.jpg"

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coEvery { commentsRepository.getCommentsForArticle(articleEntity) } returns listOf(commentEntity1, commentEntity2)
            coEvery { usersRepository.getUserEntityById(EntityID(commentAuthorId1, Users)) } returns commentAuthorEntity1
            coEvery { usersRepository.getUserEntityById(EntityID(commentAuthorId2, Users)) } returns commentAuthorEntity2
            coEvery { followingsRepository.isFollowing(commentAuthorId1.toString(), userId) } returns true
            coEvery { followingsRepository.isFollowing(commentAuthorId2.toString(), userId) } returns false

            // When
            val result = articlesService.getCommentsForArticle(userId, slug)

            // Then
            assertEquals(2, result.comments.size)

            // Verify first comment
            assertEquals(commentId1, result.comments[0].id)
            assertEquals(now.toString(), result.comments[0].createdAt)
            assertEquals(now.toString(), result.comments[0].updatedAt)
            assertEquals("Comment 1 body", result.comments[0].body)
            assertEquals("commentauthor1", result.comments[0].author.username)
            assertEquals("Comment Author 1 Bio", result.comments[0].author.bio)
            assertEquals("https://example.com/commentauthor1.jpg", result.comments[0].author.image)
            assertTrue(result.comments[0].author.following)

            // Verify second comment
            assertEquals(commentId2, result.comments[1].id)
            assertEquals(now.toString(), result.comments[1].createdAt)
            assertEquals(now.toString(), result.comments[1].updatedAt)
            assertEquals("Comment 2 body", result.comments[1].body)
            assertEquals("commentauthor2", result.comments[1].author.username)
            assertEquals("Comment Author 2 Bio", result.comments[1].author.bio)
            assertEquals("https://example.com/commentauthor2.jpg", result.comments[1].author.image)
            assertFalse(result.comments[1].author.following)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { commentsRepository.getCommentsForArticle(articleEntity) }
            coVerify { usersRepository.getUserEntityById(EntityID(commentAuthorId1, Users)) }
            coVerify { usersRepository.getUserEntityById(EntityID(commentAuthorId2, Users)) }
            coVerify { followingsRepository.isFollowing(commentAuthorId1.toString(), userId) }
            coVerify { followingsRepository.isFollowing(commentAuthorId2.toString(), userId) }
        }

    @Test
    fun testGetCommentsForArticleWithoutUser() =
        runBlocking {
            // Given
            val slug = "test-article"

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()

            val commentId = 1L
            val commentEntity = mockk<CommentEntity>()
            val now = Instant.now()

            val commentAuthorId = UUID.randomUUID()
            val commentAuthorEntity = mockk<UserEntity>()

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug

            // Set up the comment entity properties
            every { commentEntity.id } returns EntityID(commentId, Comments)
            every { commentEntity.createdAt } returns now
            every { commentEntity.updatedAt } returns now
            every { commentEntity.body } returns "Comment body"
            every { commentEntity.userId } returns EntityID(commentAuthorId, Users)

            // Set up the comment author entity
            every { commentAuthorEntity.id } returns EntityID(commentAuthorId, Users)
            every { commentAuthorEntity.username } returns "commentauthor"
            every { commentAuthorEntity.bio } returns "Comment Author Bio"
            every { commentAuthorEntity.image } returns "https://example.com/commentauthor.jpg"

            // Mock repository methods
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coEvery { commentsRepository.getCommentsForArticle(articleEntity) } returns listOf(commentEntity)
            coEvery { usersRepository.getUserEntityById(EntityID(commentAuthorId, Users)) } returns commentAuthorEntity
            coEvery { followingsRepository.isFollowing(any(), any()) } returns false

            // When
            val result = articlesService.getCommentsForArticle(null, slug)

            // Then
            assertEquals(1, result.comments.size)
            assertEquals(commentId, result.comments[0].id)
            assertEquals(now.toString(), result.comments[0].createdAt)
            assertEquals(now.toString(), result.comments[0].updatedAt)
            assertEquals("Comment body", result.comments[0].body)
            assertEquals("commentauthor", result.comments[0].author.username)
            assertEquals("Comment Author Bio", result.comments[0].author.bio)
            assertEquals("https://example.com/commentauthor.jpg", result.comments[0].author.image)
            assertFalse(result.comments[0].author.following)

            // Verify repository calls
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { commentsRepository.getCommentsForArticle(articleEntity) }
            coVerify { usersRepository.getUserEntityById(EntityID(commentAuthorId, Users)) }
        }

    @Test
    fun testGetCommentsForArticleNotFound() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "non-existent-article"

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns null

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.getCommentsForArticle(userId, slug)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
        }

    @Test
    fun testDeleteComment() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "test-article"
            val commentId = 1L

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()

            val commentEntity = mockk<CommentEntity>()

            // Set up the user entity properties
            val userUUID = UUID.fromString(userId)
            val userEntityId = mockk<EntityID<UUID>>()
            every { userEntity.id } returns userEntityId
            every { userEntityId.value } returns userUUID

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug

            // Set up the comment entity properties
            every { commentEntity.id } returns EntityID(commentId, Comments)
            every { commentEntity.userId } returns userEntityId

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coEvery { commentsRepository.getCommentsForArticle(articleEntity) } returns listOf(commentEntity)
            coJustRun { commentsRepository.deleteComment(commentEntity) }

            // When
            articlesService.deleteComment(userId, slug, commentId)

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { commentsRepository.getCommentsForArticle(articleEntity) }
            coVerify { commentsRepository.deleteComment(commentEntity) }
        }

    @Test
    fun testDeleteCommentArticleNotFound() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "non-existent-article"
            val commentId = 1L

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns null

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.deleteComment(userId, slug, commentId)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
        }

    @Test
    fun testDeleteCommentNotFound() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val slug = "test-article"
            val commentId = 999L

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()

            val existingCommentId = 1L
            val existingCommentEntity = mockk<CommentEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug

            // Set up the existing comment entity properties
            every { existingCommentEntity.id } returns EntityID(existingCommentId, Comments)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coEvery { commentsRepository.getCommentsForArticle(articleEntity) } returns listOf(existingCommentEntity)

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.deleteComment(userId, slug, commentId)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { commentsRepository.getCommentsForArticle(articleEntity) }
        }

    @Test
    fun testDeleteCommentNotAuthor() =
        runBlocking {
            // Given
            val userId = UUID.randomUUID().toString()
            val userEntity = mockk<UserEntity>()
            val commentAuthorId = UUID.randomUUID()
            val slug = "test-article"
            val commentId = 1L

            val articleId = UUID.randomUUID()
            val articleEntity = mockk<ArticleEntity>()

            val commentEntity = mockk<CommentEntity>()

            // Set up the user entity properties
            every { userEntity.id } returns EntityID(UUID.fromString(userId), Users)

            // Set up the article entity properties
            every { articleEntity.id } returns EntityID(articleId, Articles)
            every { articleEntity.slug } returns slug

            // Set up the comment entity properties
            every { commentEntity.id } returns EntityID(commentId, Comments)
            every { commentEntity.userId } returns EntityID(commentAuthorId, Users)

            // Mock repository methods
            coEvery { usersRepository.getUserEntityById(userId) } returns userEntity
            coEvery { articlesRepository.getArticleBySlug(slug) } returns articleEntity
            coEvery { commentsRepository.getCommentsForArticle(articleEntity) } returns listOf(commentEntity)

            // When/Then
            assertFailsWith<IllegalStateException> {
                articlesService.deleteComment(userId, slug, commentId)
            }

            // Verify repository calls
            coVerify { usersRepository.getUserEntityById(userId) }
            coVerify { articlesRepository.getArticleBySlug(slug) }
            coVerify { commentsRepository.getCommentsForArticle(articleEntity) }
        }
}
