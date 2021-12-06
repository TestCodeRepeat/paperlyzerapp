package com.flyingobjex.process.initialization

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.process.InitializationProcess
import io.kotest.matchers.shouldNotBe
import java.util.logging.Logger
import org.junit.Test

class InitializationProcessTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)
    private val samplePath = "../tbl_cli_sample.tsv"
    private val livePath = "../tbl_cli_full.tsv"

    val process: InitializationProcess = InitializationProcess(mongo)

    @Test
    fun `should run process with sample file`(){
        process.tsvFilePath = samplePath
        val res = process.printStats()
        res shouldNotBe null

        process.runProcess()
//        log.info("InitializationProcessTest.should run process with sample file()   = ${process.printStats()}" )
        process.printStats()
    }

//    @Test
    fun `should reset initialization process`(){
        process.printStats()
        process.reset()
        process.printStats()
    }

//    @Test
    fun `should initialize raw papers and raw authors from csv`() {
        val res = process.printStats()
        res shouldNotBe null
    }
}
