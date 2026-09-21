package me.bossm0n5t3r.configurations

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.bossm0n5t3r.articles.Articles
import me.bossm0n5t3r.articles.Comments
import me.bossm0n5t3r.articles.FavoriteArticles
import me.bossm0n5t3r.profiles.Followings
import me.bossm0n5t3r.tags.ArticleTags
import me.bossm0n5t3r.tags.Tags
import me.bossm0n5t3r.users.Users
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

interface DatabaseManager {
    val database: Database

    suspend fun <T> dbQuery(block: suspend () -> T): T

    fun createTables()

    fun dropTables()
}

class DatabaseManagerImpl : DatabaseManager {
    private fun hikariDataSource(): HikariDataSource {
        val config =
            HikariConfig().apply {
                driverClassName = "org.h2.Driver"
                jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
                maximumPoolSize = 3
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }
        return HikariDataSource(config)
    }

    override val database: Database = Database.connect(hikariDataSource())

    override suspend fun <T> dbQuery(block: suspend () -> T): T =
        suspendTransaction(db = database) {
            addLogger(StdOutSqlLogger)
            block()
        }

    private val tables =
        arrayOf(Users, Followings, Articles, FavoriteArticles, Tags, ArticleTags, Comments)

    override fun createTables() {
        transaction(this.database) { SchemaUtils.create(*tables) }
        LOGGER.info("{} createTables", LogResult.SUCCEEDED)
    }

    override fun dropTables() {
        transaction(this.database) { SchemaUtils.drop(*tables) }
        LOGGER.info("{} dropTables", LogResult.SUCCEEDED)
    }
}
