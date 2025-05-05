package me.bossm0n5t3r.configurations

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import me.bossm0n5t3r.articles.Articles
import me.bossm0n5t3r.articles.Comments
import me.bossm0n5t3r.articles.FavoriteArticles
import me.bossm0n5t3r.profiles.Followings
import me.bossm0n5t3r.tags.ArticleTags
import me.bossm0n5t3r.tags.Tags
import me.bossm0n5t3r.users.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

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
        newSuspendedTransaction(Dispatchers.IO, db = database) {
            addLogger(StdOutSqlLogger)
            block()
        }

    private val tables = arrayOf(Users, Followings, Articles, FavoriteArticles, Tags, ArticleTags, Comments)

    override fun createTables() {
        transaction(this.database) {
            SchemaUtils.create(*tables)
        }
        LOGGER.info("{} createTables", LogResult.SUCCEEDED)
    }

    override fun dropTables() {
        transaction(this.database) {
            SchemaUtils.drop(*tables)
        }
        LOGGER.info("{} dropTables", LogResult.SUCCEEDED)
    }
}
