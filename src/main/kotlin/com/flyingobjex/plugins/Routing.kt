package com.flyingobjex.plugins

import com.flyingobjex.app
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.h5

fun Application.configureRouting() {


    install(Locations) {
    }

    routing {
        get("/report") {
            log.info(".configureRouting()  /report")
            call.respondFile(app.report())
            log.info(".configureRouting()  /report !!!DONE!!!!")
        }

        get("/hello") {
            call.respondHtml {
                body {
                    h5 { +"===================" }
                    h5 { +"===================" }
                }
            }
        }

        get("/url"){
            call.respondText { app.serverUrl }
        }

        get("/") {
            call.respondText { "hello" }
        }

        // Static feature. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }

        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

        }
    }
}


class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
