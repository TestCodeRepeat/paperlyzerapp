package com.flyingobjex.csv

import com.flyingobjex.paperlyzer.parser.SJRModel
import com.flyingobjex.paperlyzer.parser.SJRank
import com.flyingobjex.paperlyzer.parser.clean
import com.flyingobjex.paperlyzer.util.JsonUtils
import com.flyingobjex.paperlyzer.util.JsonUtils.json
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import kotlinx.serialization.decodeFromString


class HIndexTest {

    val path = "src/main/resources/sjrscore.json"

    val testTitleAsme = "Journal of Energy Resources Technology, Transactions of the ASME" // only 77
    val franceTitle = "Bulletin - Societie Geologique de France"
    val bioTitle = "Magnetic Resonance Materials in Physics, Biology, and Medicine"
    val transpoTitle = "Journal of Transportation Engineering Part A: Systems"

    val sjrtDeepSea = "Deep-Sea Research Part I: Oceanographic Research Papers"
    val deepSea = "DEEP-SEA RESEARCH PART I-OCEANOGRAPHIC RESEARCH PAPERS"


    val sjrCanada = "Canadian Journal of Fisheries and Aquatic Sciences"
    val canada = "CANADIAN JOURNAL OF FISHERIES AND AQUATIC SCIENCES"

    val dbNatureResource = "NATURAL RESOURCES RESEARCH"

    val sjrModel = SJRModel()

    @Test
    fun `should match deepSea journal name from sjr rankings`() {
        sjrModel.matchJournalTitleToSJRank(deepSea) shouldNotBe null
        sjrModel.matchJournalTitleToSJRank(canada) shouldNotBe null
    }

    @Test
    fun `should match raw journal titles`(){
        clean(sjrtDeepSea) shouldBe clean(deepSea)
        clean(sjrCanada) shouldBe clean(canada)
    }

//    @Test
    fun `should match dbNatureResource journal name from sjr rankings`() {
        sjrModel.matchJournalTitleToSJRank(dbNatureResource) shouldNotBe null
    }

//    @Test
    fun `should match basic journal names from sjr rankings`() {
        sjrModel.matchJournalTitleToSJRank(transpoTitle) shouldNotBe null
        sjrModel.matchJournalTitleToSJRank(bioTitle) shouldNotBe null
        sjrModel.matchJournalTitleToSJRank(testTitleAsme) shouldNotBe null
        sjrModel.matchJournalTitleToSJRank(franceTitle) shouldNotBe null
    }

    //    @Test
    fun `should hello world`() {
        println("hello world :: should hello worl :: ")
        val fileContent = this::class.java.classLoader.getResource("sjrscore.json").readText()
        val res = json.decodeFromString<ArrayList<SJRank>>(fileContent)
        res shouldNotBe null
        res.size shouldBeGreaterThan 30000
    }

    //    @Test
    fun `should convert h-index csv file from to json file`() {
        val file = JsonUtils.loadFile(path)
        val res = json.decodeFromString<ArrayList<SJRank>>(file)
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

