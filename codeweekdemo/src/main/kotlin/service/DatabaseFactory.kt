package main.kotlin.service


import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import main.kotlin.model.TradeIdeas
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {

        // Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        Database.connect(hikari())
        transaction {
            create(TradeIdeas)
            TradeIdeas.insert {
                it[ticker] = "AAPL US"
                it[quantity] = 27
                it[dateUpdated] = System.currentTimeMillis()
            }

            TradeIdeas.insert {
                it[ticker] = "MSFT US"
                it[quantity] = 14
                it[dateUpdated] = System.currentTimeMillis()
            }
        }
    }

    private fun hikari(): HikariDataSource {

        val config = HikariConfig()
        config.driverClassName = "org.h2.Driver"
        config.jdbcUrl = "jdbc:h2:mem:test"
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"

        config.validate()

        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(
        block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}