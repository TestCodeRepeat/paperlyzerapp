package com.flyingobjex

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.control.MainCoordinator
import com.flyingobjex.paperlyzer.control.StatsController
import com.flyingobjex.paperlyzer.entity.SemanticScholarPaper
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.SemanticScholarPaperRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.util.setMongoDbLogsToErrorOnly
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Before
import org.junit.Test
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

class CitationTest {
    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)
    private val samplePath = "../tbl_cli_sample.tsv"
    private val coordinator = MainCoordinator(mongo, samplePath)
    private val stats = StatsController(mongo)
    private val wosRepo = WoSPaperRepository(mongo)
    private val authorRepo = AuthorRepository(mongo)
    private val ssRepo = SemanticScholarPaperRepository(mongo)
    private val app = PaperlyzerApp(mongo)

    private val doiWos = "10.1016/j.baae.2017.07.002"
    private val doiSSc = "10.1016/J.BAAE.2017.07.002"

    init {
        setMongoDbLogsToErrorOnly()
    }

    @Before
    fun before() {
        setMongoDbLogsToErrorOnly()
    }

    @Test
    fun `mongo should match on case-insensitive matches`() {
        setMongoDbLogsToErrorOnly()

        val wos = mongo.genderedPapers.findOne(WosPaper::doi eq doiWos)!!
        assertEquals(doiWos, wos.doi)

        val ss = mongo.ssPapers.findOne(SemanticScholarPaper::doi eq doiSSc)!!
        assertEquals(doiSSc, ss.doi)

        val res = ssRepo.paperByDoi(doiWos)
        assertEquals(doiWos.toLowerCase(), res?.doi?.toLowerCase())

        assertNull(ssRepo.paperByDoi("nah"))

    }

    @Test
    fun `app should start citation process`() {
        wosRepo.resetCitationProcessed()
        app.runProcess()
        app.process.printStats()
        log.info("CitationTest.app should start citation process()  DONE !!!")
    }

    //    @Test
    fun `app should print stats`() {

    }

    //    @Test
    fun `should apply citations to gendered wos papers`() {
//        wosRepo.resetCitationProcessed()
        coordinator.applyCitationsToGenderedPapers()

        val processed = wosRepo.mongo.genderedPapers.countDocuments(WosPaper::citationsProcessed eq true)
        log.info("CitationTest.should apply citations to gendered wos papers()  procesed = ${processed}")
        val count = wosRepo.unprocessedCitationsCount()
        log.info("Coordinator.applyCitationsToGenderedPapers()  unprocessed = ${count}")
    }

    //    @Test
    fun `should reset wos papers citations processed to false `() {
        val a = wosRepo.mongo.genderedPapers.countDocuments(WosPaper::citationsProcessed eq true)
        log.info("CitationTest.should reset wos papers citations processed to false ()  a = ${a}")

        wosRepo.resetCitationProcessed()

        val b = wosRepo.mongo.genderedPapers.countDocuments(WosPaper::citationsProcessed eq true)
        log.info("CitationTest.should reset wos papers citations processed to false ()  b = ${b}")

        val c = wosRepo.mongo.genderedPapers.countDocuments(WosPaper::citationsProcessed eq false)
        log.info("CitationTest.should reset wos papers citations processed to false ()  c = ${c}")

    }

}
