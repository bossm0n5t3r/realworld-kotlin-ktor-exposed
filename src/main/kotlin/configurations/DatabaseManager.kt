package me.bossm0n5t3r.configurations

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import me.bossm0n5t3r.users.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
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

    override suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO, db = database) { block() }

    override fun createTables() {
        transaction(this.database) {
            SchemaUtils.create(Users)
        }
        LOGGER.info("{} createTables", LogResult.SUCCEEDED)
    }

    override fun dropTables() {
        transaction(this.database) {
            SchemaUtils.drop(Users)
        }
        LOGGER.info("{} dropTables", LogResult.SUCCEEDED)
    }
}
