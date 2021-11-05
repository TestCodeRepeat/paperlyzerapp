package com.flyingobjex.plugins

import com.flyingobjex.app
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import io.ktor.application.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import io.ktor.websocket.WebSockets
import java.time.Duration
import java.util.*

enum class SocketAction {
    START, STOP, STATS, RESET, NA;

    companion object {
        fun toType(value: String): SocketAction {
            return when (value) {
                "START" -> START
                "STOP" -> STOP
                "STATS" -> STATS
                "RESET" -> RESET
                else -> NA
            }
        }
    }
}

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/allthethings") { // websocketSession
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        val response = app.handleSocketCommand(SocketAction.toType(text.uppercase(Locale.getDefault())), outgoing)
                        outgoing.send(Frame.Text("YOU SAID: $text"))
                        outgoing.send(Frame.Text("Response: $response"))
                        if (text.equals("bye", ignoreCase = true)) {
                            println("Sockets.kt :: ByE !!  CLOSING !!!! CANCELLING JOBS !!!!! CLOSING§ :: ")
                            app.process.cancelJobs()
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        }
                    }
                    is Frame.Close -> {
                        println("Sockets.kt :: CLOSING !!!! CANCELLING JOBS !!!!! CLOSING§ :: ")
                        app.process.printStats(outgoing)
                        app.process.cancelJobs()
                    }
                }
            }
        }
    }
}

suspend fun DefaultClientWebSocketSession.inputMessages() {
    while (true) {
        val message = readLine() ?: ""
        if (message.equals("exit", true)) return
        try {
            send(message)
        } catch (e: Exception) {
            println("Error while sending: " + e.localizedMessage)
            return
        }
    }
}

suspend fun DefaultClientWebSocketSession.outputMessages() {
    try {
        for (message in incoming) {
            message as? Frame.Text ?: continue
            println(message.readText())
        }
    } catch (e: Exception) {
        println("Error while receiving: " + e.localizedMessage)
    }
}
