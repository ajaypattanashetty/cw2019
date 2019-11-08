package main.kotlin.service

import main.kotlin.model.*
import org.jetbrains.exposed.sql.*
import main.kotlin.service.DatabaseFactory.dbQuery

class TradeIdeaService {
    private val listeners = mutableMapOf<Int, suspend (Notification<TradeIdea?>) -> Unit>()

    fun addChangeListener(id: Int, listener: suspend (Notification<TradeIdea?>) -> Unit) {
        listeners[id] = listener
    }

    fun removeChangeListener(id: Int) = listeners.remove(id)

    private suspend fun onChange(type: ChangeType, id: Int, entity: TradeIdea?=null) {
        listeners.values.forEach {
            it.invoke(Notification(type, id, entity))
        }
    }

    suspend fun getAllTradeIdeas(): List<TradeIdea> = dbQuery {
        TradeIdeas.selectAll().map { toTradeIdea(it) }
    }

    suspend fun getTradeIdea(id: Int): TradeIdea? = dbQuery {
        TradeIdeas.select {
            (TradeIdeas.id eq id)
        }.mapNotNull { toTradeIdea(it) }
            .singleOrNull()
    }

    suspend fun updateTradeIdea(tradeIdea: NewTradeIdeas): TradeIdea? {
        val id = tradeIdea.id
        return if (id == null) {
            addTradeIdea(tradeIdea)
        } else {
            dbQuery {
                TradeIdeas.update({ TradeIdeas.id eq id }) {
                    it[ticker] = tradeIdea.ticker
                    it[quantity] = tradeIdea.quantity
                    it[dateUpdated] = System.currentTimeMillis()
                }
            }

            getTradeIdea(id).also {
                onChange(ChangeType.UPDATE, id, it)
            }
        }
    }

    suspend fun addTradeIdea(tradeIdea: NewTradeIdeas): TradeIdea {
        var key = 0
        dbQuery {
            key = (TradeIdeas.insert {
                it[ticker] = tradeIdea.ticker
                it[quantity] = tradeIdea.quantity
                it[dateUpdated] = System.currentTimeMillis()
            } get TradeIdeas.id)
        }

        return getTradeIdea(key)!!.also {
            onChange(ChangeType.CREATE, key, it)
        }
    }

    suspend fun deleteTradeIdea(id: Int): Boolean {
        return dbQuery {
            TradeIdeas.deleteWhere { TradeIdeas.id eq id } > 0
        }.also {
            if(it) onChange(ChangeType.DELETE, id)
        }
    }

    private fun toTradeIdea(row: ResultRow): TradeIdea =
        TradeIdea(
            id = row[TradeIdeas.id],
            ticker = row[TradeIdeas.ticker],
            quantity = row[TradeIdeas.quantity],
            dateUpdated = row[TradeIdeas.dateUpdated]
        )
}