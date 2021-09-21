package com.flyingobjex

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.control.SSPaperController
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.repo.WosPaperId
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Before
import org.litote.kmongo.*
import org.slf4j.LoggerFactory

class SSControllerTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(true)
    private val wosPaperRepo = WoSPaperRepository(mongo, ::logMessage)
    private val ssPaperController = SSPaperController(mongo, ::logMessage)
    private val app = PaperlyzerApp(mongo)

    val batchSize = 50

    @Before
    fun before() {
        println("SSCoordinatorTest.kt :: SSCoordinatorTest :: before ()")
        (LoggerFactory.getLogger("org.mongodb.driver") as ch.qos.logback.classic.Logger).setLevel(ch.qos.logback.classic.Level.OFF)
    }

//    @Test
    fun `should processes api batches in parallel by group`() {
        wosPaperRepo.resetSsProcessed()
//        val res = app.process.processedBatchGroup(5, 20)
        print("done")
    }

    //    @Test
    fun `should get projection of unprocessed papers by doi`() {
        val res = wosPaperRepo.getUnprocessedPapersAsPaperIds(20)
        assertEquals(20, res.size)
    }


    //    @Test
    fun `should query for batch of unprocessed papers`() {
        val res = mongo.genderedPapers.aggregate<WosPaperId>(
            match(
                or(
                    WosPaper::ssProcessed eq null,
                    WosPaper::ssProcessed ne true,
                ),
                and(
                    WosPaper::ssFailed ne true,
                    WosPaper::doi ne "NA"
                )
            ),
            project(WosPaperId::doi from WosPaper::doi),
            limit(batchSize)
        ).toList()

        assertEquals(50, res.size)
    }

    //    @Test
    fun `should run a batch as long as the unprocessed wosPaper count is less than the goal`() {
        val res = app.process.shouldContinueProcess()
        assertTrue(res)
    }

    //    @Test
    fun `should check number of remaining unprocessed wos papers`() {
        val res = ssPaperController.getUnprocessedWosPaperCount()
        assertTrue(res > 100)
    }

    //    @Test
    fun `reset ssProcessed on genderedPapers collection`() {
        mongo.resetIndexes()
        wosPaperRepo.resetSsProcessed()

    }

    //    @Test
    fun `create a loop for grouped batches of 20`() {
        var n = 0
        for (i in 0..10) {
            val start = n
            val end = n + batchSize - 1
            println("SSPaperController.kt :: SSPaperController :: start = " + start)
            println("SSPaperController.kt :: SSPaperController :: end = " + end)
            n += batchSize
        }
    }

    //    @Test
//    fun `run batch of semantic scholar requests`() = runBlocking {
//        val res = ssPaperController.getUnprocessedPapers_OLD(500)
//        print(res)
//        assertTrue(res)
//        print(res)
//
//    }

    //    @Test
    fun `build Semantic Scholar papers table`() {

    }


}
