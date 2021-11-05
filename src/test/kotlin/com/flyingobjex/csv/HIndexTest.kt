package com.flyingobjex.csv

import java.io.File
import org.junit.Test


class HIndexTest {

    val testTitleAsme = "Journal of Energy Resources Technology, Transactions of the ASME" // only 77
    val franceTitle = "Bulletin - Societie Geologique de France"
    val bioTitle = "Magnetic Resonance Materials in Physics, Biology, and Medicine"
    val transpoTitle = "Journal of Transportation Engineering Part A: Systems"

//    fun `should load h-index csv`(){
////        val file =
//    }
//
//    fun `should handle title separated by comma`(){
//
//    }

    @Test
    fun `should hello worl`(){
        println("hello world :: should hello worl :: ")
    }

    @Test
    fun `should convert h-index csv file from to json file`(){
        File("testfile.json").writeText("thing")
//        val res = CSVHIndexParser.csfToHIndex("")
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

