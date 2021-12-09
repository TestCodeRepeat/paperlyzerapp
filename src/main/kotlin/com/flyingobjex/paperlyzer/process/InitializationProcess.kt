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

    var tsvFilePath: String = "no_path_specified"
        set(value) {
            field = value
        }

    override fun init() {
        TODO("Not yet implemented")
    }

    override fun name(): String = "Initialization Process"

    override fun runProcess() {
        val rawCsvPapers = CSVParser.csvFileToRawPapers(tsvFilePath)
        wosRepo.insertRawCsvPapers(rawCsvPapers)

        val authorsFromPaperTable = wosRepo.getAllRawAuthors()
        authorRepo.insertManyAuthors(authorsFromPaperTable)
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
        println("${Date()} resetRawAuthors()")
        authorRepo.clearRawData()
    }

    override fun type(): ProcessType = ProcessType.Initialization

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
