package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.parser.CSVParser
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import io.ktor.http.cio.websocket.*
import java.util.*
import java.util.logging.Logger
import kotlinx.coroutines.channels.SendChannel

class InitializationProcess(mongo: Mongo) : IProcess {

    val log: Logger = Logger.getAnonymousLogger()
    private val wosRepo = WoSPaperRepository(mongo)
    private val authorRepo = AuthorRepository(mongo)

    private val samplePath = "../tbl_cli_sample.tsv"
    private val livePath = "../tbl_cli_full.tsv"

    val tsvFilePath: String = samplePath

    override fun init() {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        TODO("Not yet implemented")
    }

    override fun runProcess() {
        val rawCsvPapers = CSVParser.csvFileToRawPapers(tsvFilePath)
        wosRepo.insertRawCsvPapers(rawCsvPapers)

        CSVParser.csvFileToAuthors(tsvFilePath)

    }

    override fun shouldContinueProcess(): Boolean {
        TODO("Not yet implemented")
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val totalRawPapers = wosRepo.mongo.rawPaperFullDetails.countDocuments()
        val totalRawAuthors = authorRepo.mongo.rawAuthors.countDocuments()
        val message = "InitializationProcess.printStats()  totalRawPapers = ${totalRawPapers}" +
            "\n InitializationProcess.printStats()  totalRawAuthors = ${totalRawAuthors}\" +"
        log.info(message)
        return (message)
    }

    override fun cancelJobs() {
        TODO("Not yet implemented")
    }

    override fun reset() {
        TODO("Not yet implemented")
        println("${Date()} resetRawAuthors()")
        authorRepo.resetRawAuthors()
        authorRepo.mongo.resetIndexes()
    }

    override fun type(): ProcessType {
        TODO("Not yet implemented")

    }

    private fun resetForBuildRawAuthorTable() {
        println("${Date()} resetRawAuthors()")
        authorRepo.resetRawAuthors()
        println("${Date()} authorTableRepo.buildAuthorTableInParallel()")
        authorRepo.mongo.resetIndexes()

    }

    fun runParseCsvToRawPapers(): List<WosPaper> {
        val rawCsvPapers = CSVParser.csvFileToRawPapers(tsvFilePath)
        wosRepo.insertRawCsvPapers(rawCsvPapers)
        return wosRepo.getAllRawPapers()
    }

}
