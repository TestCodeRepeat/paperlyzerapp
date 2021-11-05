package com.flyingobjex

import ch.qos.logback.classic.Logger
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.plugins.configureRouting
import com.flyingobjex.plugins.configureSerialization
import com.flyingobjex.plugins.configureSockets
import io.ktor.application.*
import io.ktor.server.engine.*
import org.slf4j.LoggerFactory


val app = PaperlyzerApp(Mongo(true))

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {

    (LoggerFactory.getLogger("org.mongodb.driver") as Logger).setLevel(ch.qos.logback.classic.Level.OFF)

    (environment as ApplicationEngineEnvironment).connectors.forEach { connector ->
        println("${connector.host}:${connector.port}")
        app.setServerUrl("${connector.host}:${connector.port}")
    }

    configureRouting()
    configureSerialization()
    configureSockets()

}

