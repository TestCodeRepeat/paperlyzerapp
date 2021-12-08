package com.flyingobjex.process.ssauthor

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.process.SemanticScholarAuthorProcess
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.litote.kmongo.eq

class SemanticScholarAuthorProcessTest {

    val mongo = Mongo()
    val authorRepo = SemanticScholarAuthorRepo(mongo)
    val process = SemanticScholarAuthorProcess(mongo)

    //    @Test
    fun `should run process for batch of 10`() {
        process.reset()
        process.runProcess()

        process.printStats()
    }

    @Test
    fun `process should print stats`() {
        val res = process.printStats()
        println("SemanticScholarAuthorProcessTest.kt :: process should print stats() :: res = $res")
    }

    //    @Test
    fun `process should reset ss author collection`() {
        process.reset()
        val res = mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessed eq false)
        res shouldBeGreaterThan 100000
    }

    @Test
    fun `should get unprocessed raw wos papers (no Semantic Scholar Author data)`() {
        val res = authorRepo.getUnprocessedRawPapers(10)
        res shouldNotBe null
    }
}
