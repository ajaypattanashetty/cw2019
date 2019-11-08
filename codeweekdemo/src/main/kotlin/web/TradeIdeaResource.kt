package main.kotlin.web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.receiveOrNull
import main.kotlin.model.NewTradeIdeas
import main.kotlin.service.TradeIdeaService

fun Route.tradeIdea(tradeIdeaService: TradeIdeaService) {

    route("/tradeIdea") {

        get("/") {
            call.respond(tradeIdeaService.getAllTradeIdeas())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalStateException("Must provide id");
            val tradeIdea = tradeIdeaService.getTradeIdea(id)
            if (tradeIdea == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(tradeIdea)
        }

        post("/") {
            val tradeIdea = call.receive<NewTradeIdeas>()
            call.respond(HttpStatusCode.Created, tradeIdeaService.addTradeIdea(tradeIdea))
        }

        put("/") {
            val tradeIdea = call.receive<NewTradeIdeas>()
            val updated = tradeIdeaService.updateTradeIdea(tradeIdea)
            if (updated == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK, updated)
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalStateException("Must provide id");
            val removed = tradeIdeaService.deleteTradeIdea(id)
            if (removed) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound)
        }
    }



    val mapper = jacksonObjectMapper().apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    webSocket("/updates") {
        try {
            tradeIdeaService.addChangeListener(this.hashCode()) {
                outgoing.send(Frame.Text(mapper.writeValueAsString(it)))
            }

            while (true) {
                incoming.receiveOrNull() ?: break
            }

        } finally {
            tradeIdeaService.removeChangeListener(this.hashCode())
        }
    }

}
