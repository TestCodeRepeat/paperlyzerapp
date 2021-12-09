package com.flyingobjex.process.ssauthor

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.process.SsApiAuthorDetailsProcess
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import org.junit.Test

class SsApiAuthorDetailsProcessTest {

    val mongo = Mongo()
    val authorRepo = SemanticScholarAuthorRepo(mongo)
    val process = SsApiAuthorDetailsProcess(mongo)

    private val app = PaperlyzerApp(mongo)

    @Test
    fun `process should run for about 10k records`(){

    }

    @Test
    fun `process should reset`(){
        println("SsApiAuthorDetailsProcessTest.kt :: process should reset() :: process.printStats() = ${process.printStats()}")
        process.reset()
        println("SsApiAuthorDetailsProcessTest.kt :: process should reset() :: process.printStats() = ${process.printStats()}")
    }
}
