package com.flyingobjex.paperlyzer.control

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.parser.CSVParser
import com.flyingobjex.paperlyzer.process.InitializationProcess
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.JournalTableRepo
import com.flyingobjex.paperlyzer.repo.SemanticScholarPaperRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.usecase.GenderedAuthorUseCase
import java.util.*
import java.util.logging.Logger
import org.slf4j.LoggerFactory

class MainCoordinator(val mongo: Mongo, val tsvFilePath: String) {
    val log: Logger = Logger.getAnonymousLogger()
    private val wosRepo = WoSPaperRepository(mongo)
    private val authorRepo = AuthorRepository(mongo)
    private val journalRepo = JournalTableRepo(mongo)
    private val ssPaperRepo = SemanticScholarPaperRepository(mongo)
    private val genderedAuthorUseCase = GenderedAuthorUseCase(mongo)

    val initProcess: InitializationProcess = InitializationProcess(mongo)

    init {
        (LoggerFactory.getLogger("org.mongodb.driver") as ch.qos.logback.classic.Logger).setLevel(ch.qos.logback.classic.Level.OFF)
    }


    /** WIP  */
    /** Add Semantic Scholar Years Published to Author Table **/
    /** WIP  */
    fun applySsYearsPublished() {
        val batchSize = 50
        val ssUnprocessedAuthors = authorRepo.getSsUnprocessedAuthors(batchSize)
        ssUnprocessedAuthors.parallelStream().forEach { author ->
            val matchingSsPapers = ssPaperRepo.papersForAuthor(author)

        }
    }

    /** Apply Citations  */
    /** For each gendered WoS paper, fetch corresponding SS paper by DOI
     *      - apply # of citations to WoS paper
     *      -
     * */
    fun applyCitationsToGenderedPapers() {
        val batchSize = 500
        val wosUnprocessedPapers = wosRepo.getUnprocessedByCitations(batchSize)
        wosUnprocessedPapers.parallelStream().forEach { wosPaper ->
            ssPaperRepo.paperByDoi(wosPaper.doi)?.let { matchingPaper ->
                wosRepo.updateCitationsCount(
                    wosPaper,
                    matchingPaper.numCitedBy ?: 0,
                    matchingPaper.influentialCitationCount ?: 0
                )
            } ?: run {
                log.warning("Coordinator.applyCitationsToGenderedPapers()  NO MATCHING PAPER wosPaper.doi = ${wosPaper.doi}")
            }
        }
    }


    /** Paper Table */
    fun applyGendersToPaperTable(batchSize: Int) {
        val papers: List<WosPaper> = wosRepo.getPapersWithAuthors(batchSize)
        val res = wosRepo.applyGenderToPaperAuthors(papers)
        print(res)
    }

    /** Journal Table */
    fun buildJournalTable() {
        val batchSize = 500000
        journalRepo.buildJournalTableInParallel(batchSize)
    }

    fun resetJournalTable() {
        mongo.clearJournals()
        mongo.resetIndexes()
    }

    /** Gendered Author Table */
    fun buildGenderedAuthorsTable(batchSize: Int) {
        genderedAuthorUseCase.buildGenderedAuthorsTable(batchSize)
    }

//    /** Author Table */
//    fun buildAuthorTable(batchSize: Int) {
//        authorRepo.buildAuthorTableInParallel(batchSize)
//    }

    fun resetForAuthorTable() {
        println("${Date()} clearAuthors()")
        mongo.clearAuthors()
        println("${Date()} resetRawAuthors()")
        authorRepo.resetRawAuthors()
        mongo.resetIndexes()

    }

    /** Raw Author Table */
    fun runParseRawAuthorTableFromRawPapers(): List<Author> {
        val authorsFromPaperTable = wosRepo.getAllRawAuthors()
        authorRepo.insertManyAuthors(authorsFromPaperTable)
        return authorsFromPaperTable
    }

    fun resetForBuildRawAuthorTable() {
        println("${Date()} resetRawAuthors()")
        authorRepo.resetRawAuthors()
        println("${Date()} authorTableRepo.buildAuthorTableInParallel()")
        mongo.resetIndexes()
    }

    fun runParseCsvToRawPapers(): List<WosPaper> {
        val rawCsvPapers = CSVParser.csvFileToRawPapers(tsvFilePath)
        wosRepo.insertRawCsvPapers(rawCsvPapers)
        return wosRepo.getAllRawPapers()
    }
}
