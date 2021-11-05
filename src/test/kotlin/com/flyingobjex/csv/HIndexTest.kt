package com.flyingobjex.csv

import com.flyingobjex.module
import com.flyingobjex.paperlyzer.parser.CSVTopicParser
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.encodeToString

class HIndexTest {

    val testTitleAsme = "Journal of Energy Resources Technology, Transactions of the ASME" // only 77
    val franceTitle = "Bulletin - Societie Geologique de France"
    val bioTitle = "Magnetic Resonance Materials in Physics, Biology, and Medicine"
    val transpoTitle = "Journal of Transportation Engineering Part A: Systems"

    fun `should load h-index csv`(){
//        val file =
    }

    fun `should handle title separated by comma`(){

    }

    fun `should load h-index csv file from static resource`(){

    }

    @Test
    fun `should download hindex csv file`() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/static/hindex.csv").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(HttpStatusCode.OK, response.status())

            }
        }
    }

//    @Test
    fun testRoot() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("hello", response.content)
            }
        }
    }
}

