package com.flyingobjex.csv

import com.flyingobjex.paperlyzer.parser.HIndexParser
import com.flyingobjex.paperlyzer.parser.SJRJournal
import com.flyingobjex.paperlyzer.util.JsonUtils
import com.flyingobjex.paperlyzer.util.JsonUtils.json
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import kotlinx.serialization.decodeFromString


class HIndexTest {

    val testTitleAsme = "Journal of Energy Resources Technology, Transactions of the ASME" // only 77
    val franceTitle = "Bulletin - Societie Geologique de France"
    val bioTitle = "Magnetic Resonance Materials in Physics, Biology, and Medicine"
    val transpoTitle = "Journal of Transportation Engineering Part A: Systems"

    val path = "docs/sjrscore.json"

//    fun `should load h-index csv`(){
////        val file =
//    }
//
//    fun `should handle title separated by comma`(){
//
//    }

    @Test
    fun `should hello world`(){
        println("hello world :: should hello worl :: ")
    }

    @Test
    fun `should convert h-index csv file from to json file`(){
        val file = JsonUtils.loadFile(path)
        val res = json.decodeFromString<ArrayList<SJRJournal>>(file)
        res shouldNotBe null
        res.size shouldBeGreaterThan 30000
    }

////    @Test
//    fun `should init hindex model by loading csv file`(){
//        withTestApplication(Application::module) {
//            handleRequest(HttpMethod.Get, "/init").apply {
//
//            }
//        }
//    }
//
////    @Test
//    fun `should download hindex csv file`() {
//        withTestApplication(Application::module) {
//            handleRequest(HttpMethod.Get, "/docs/hindex.csv").apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals(HttpStatusCode.OK, response.status())
//            }
//        }
//    }
//
////    @Test
//    fun testRoot() {
//        withTestApplication(Application::module) {
//            handleRequest(HttpMethod.Get, "/").apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//                assertEquals("hello", response.content)
//            }
//        }
//    }
}

