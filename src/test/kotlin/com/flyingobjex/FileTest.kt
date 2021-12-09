package com.flyingobjex

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.assertEquals
import org.junit.Test

class FileTest {

    private val mongo = Mongo(false)
    private val wosRepo = WoSPaperRepository(mongo)

    private val app = PaperlyzerApp(mongo)


    @Test
    fun `app should save tsv as temporary file`(){
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/static/topics.tsv").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                println("FileTest.kt :: should load a file from server() :: response = " + response.content)
                app.initProcess(ProcessType.Discipline)
            }
        }
    }

//    @Test
    fun `should load a file from server`() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/static/topics.tsv").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                println("FileTest.kt :: should load a file from server() :: response = " + response.content)
            }
        }
    }

//    @Test
    fun testRoot() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}
