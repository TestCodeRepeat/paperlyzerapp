package com.flyingobjex.process.ssauthor

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.process.SsApiAuthorDetailsProcess
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import io.kotest.matchers.shouldBe
import org.junit.Test

const val TOTAL_WOS_PAPERS = 399893

class SsApiAuthorDetailsProcessTest {

    val mongo = Mongo()
    val authorRepo = SemanticScholarAuthorRepo(mongo)
    val process = SsApiAuthorDetailsProcess(mongo)

    private val app = PaperlyzerApp(mongo)


    //    @Test
    fun `process should print stats`() {
        println(
            "SsApiAuthorDetailsProcessTest.kt :: process should print stats() :: STATS =" +
                "\n ${process.printStats()}"
        )
    }

    @Test
    fun `process should run for 1000 records`() {
        app.process.type() shouldBe ProcessType.SsApiAuthor
        app.manuallySetUnprocessedRecordsGoal(TOTAL_WOS_PAPERS - 1000)
        app.process.reset()
        app.process.printStats()

        app.start()

        app.process.printStats()
    }

    //    @Test
    fun `when the process runs, it should fetch SsAuthor Details and store them`() {
        process.reset()
        process.runProcess()
        process.printStats()
    }

    //    @Test
    fun `process should reset`() {
        println("SsApiAuthorDetailsProcessTest.kt :: process should reset() :: process.printStats() = ${process.printStats()}")
        process.reset()
        println("SsApiAuthorDetailsProcessTest.kt :: process should reset() :: process.printStats() = ${process.printStats()}")
    }
}
