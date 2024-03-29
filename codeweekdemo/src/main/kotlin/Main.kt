package main.kotlin



import com.fasterxml.jackson.databind.SerializationFeature

import io.ktor.application.Application

import io.ktor.application.install

import io.ktor.features.CallLogging

import io.ktor.features.ContentNegotiation

import io.ktor.features.DefaultHeaders

import io.ktor.jackson.jackson

import io.ktor.routing.Routing

import io.ktor.server.engine.commandLineEnvironment

import io.ktor.server.engine.embeddedServer

import io.ktor.server.netty.Netty

import io.ktor.websocket.WebSockets

import main.kotlin.service.DatabaseFactory

import main.kotlin.service.TradeIdeaService

import main.kotlin.web.tradeIdea

import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun Application.module() {

    install(DefaultHeaders)

    install(CallLogging)

    install(WebSockets)



    install(ContentNegotiation) {

        jackson {

            configure(SerializationFeature.INDENT_OUTPUT, true)

        }

    }



    DatabaseFactory.init()



    val tradeIdeaService = TradeIdeaService()



    install(Routing) {

        tradeIdea(tradeIdeaService)
    }
}



fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}
