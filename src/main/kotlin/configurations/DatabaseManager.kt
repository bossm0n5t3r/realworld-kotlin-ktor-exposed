package me.bossm0n5t3r.configurations

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import me.bossm0n5t3r.users.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface DatabaseManager {
    val database: Database

    suspend fun <T> dbQuery(block: suspend () -> T): T

    suspend fun createTables()

    suspend fun dropTables()
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

    override suspend fun createTables() {
        dbQuery {
            SchemaUtils.create(Users)
        }
    }

    override suspend fun dropTables() {
        dbQuery {
            SchemaUtils.drop(Users)
        }
    }
}
