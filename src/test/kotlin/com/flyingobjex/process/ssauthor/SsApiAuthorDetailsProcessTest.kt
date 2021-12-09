package com.flyingobjex.process.ssauthor

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.process.SsApiAuthorDetailsProcess
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import io.kotest.matchers.shouldBe
import org.junit.Test

class SsApiAuthorDetailsProcessTest {

    val mongo = Mongo()
    val authorRepo = SemanticScholarAuthorRepo(mongo)
    val process = SsApiAuthorDetailsProcess(mongo)

    private val app = PaperlyzerApp(mongo)

//    @Test
    fun `process should run for 10 records`(){
        app.process.type() shouldBe ProcessType.SsApiAuthor
        app.process.reset()
        app.process.printStats()

        app.process.runProcess()
    }

    @Test
    fun `when the process runs, it should fetch SsAuthor Details and store them`(){
        process.reset()
        process.runProcess()
        process.printStats()
    }

//    @Test
    fun `process should reset`(){
        println("SsApiAuthorDetailsProcessTest.kt :: process should reset() :: process.printStats() = ${process.printStats()}")
        process.reset()
        println("SsApiAuthorDetailsProcessTest.kt :: process should reset() :: process.printStats() = ${process.printStats()}")
    }
}
