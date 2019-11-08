package main.kotlin.model

import org.jetbrains.exposed.sql.Table

object TradeIdeas : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val ticker = varchar("ticker", 255)
    val quantity = integer("quantity")
    val dateUpdated = long("dateUpdated")
}

data class TradeIdea(
    val id: Int,
    val ticker: String,
    val quantity: Int,
    val dateUpdated: Long
)

data class NewTradeIdeas(
    val id: Int?,
    val ticker: String,
    val quantity: Int
)