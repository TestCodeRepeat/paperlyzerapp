package com.flyingobjex.process.ssauthor

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.process.SsAuthorToRawPaperProcess
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.litote.kmongo.eq

class SsAuthorToRawPaperProcessTest {

    val mongo = Mongo()
    val authorRepo = SemanticScholarAuthorRepo(mongo)
    val process = SsAuthorToRawPaperProcess(mongo)
    private val app = PaperlyzerApp(mongo)

    @Test
    fun `app should process 10k records`(){
        app.process.type() shouldBe ProcessType.SsAuthor
        app.process.reset()
        app.start()
    }

//    @Test
    fun `should run process for batch of 10`() {
        process.printStats()
        process.reset()
        process.runProcess()
        process.printStats()

        val processed = mongo.rawPaperFullDetails.find(WosPaper::ssAuthorProcessedStep1 eq true).toList()
        processed.size shouldBe 10
    }

//    @Test
    fun `process should print stats`() {
        val res = process.printStats()
        println("SemanticScholarAuthorProcessTest.kt :: process should print stats() :: res = $res")
    }

    //    @Test
    fun `process should reset ss author collection`() {
        process.reset()
        val res = mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessedStep1 eq false)
        res shouldBeGreaterThan 100000
    }

//    @Test
    fun `should get unprocessed raw wos papers (no Semantic Scholar Author data)`() {
        val res = authorRepo.getUnprocessedRawPapers(10)
        res shouldNotBe null
    }


}
