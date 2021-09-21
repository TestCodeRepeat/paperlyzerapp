package com.flyingobjex

import ch.qos.logback.classic.Logger
import com.flyingobjex.plugins.configureRouting
import com.flyingobjex.plugins.configureSerialization
import com.flyingobjex.plugins.configureSockets
import io.ktor.application.*
import org.slf4j.LoggerFactory


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {

    (LoggerFactory.getLogger("org.mongodb.driver") as Logger).setLevel(ch.qos.logback.classic.Level.OFF)

    configureRouting()
    configureSerialization()
    configureSockets()

}

