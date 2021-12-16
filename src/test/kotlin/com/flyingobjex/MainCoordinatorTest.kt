package com.flyingobjex

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.MainCoordinator
import com.flyingobjex.paperlyzer.control.StatsController
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.util.setMongoDbLogsToErrorOnly
import org.junit.Test
import java.util.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class MainCoordinatorTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)
    private val samplePath = "../tbl_cli_sample.tsv"
    private val livePath = "../tbl_cli_full.tsv"
    private val coordinator = MainCoordinator(mongo, samplePath)
    private val stats = StatsController(mongo)
    private val paperRepo = WoSPaperRepository(mongo)
    private val authorRepo = AuthorRepository(mongo)

    init {
        setMongoDbLogsToErrorOnly()
    }

    //    @Test
    fun `apply genders to authors in paper`() {
        val resetTime = measureTimeMillis {
            paperRepo.resetPaperTableGenderInfo()
        }

        val res = coordinator.applyGendersToPaperTable(100)
        print(res)
    }

    //    @Test
    fun `get papers with authors projection`() {
        val resetTime = measureTimeMillis {
            paperRepo.resetPaperTableGenderInfo()
        }
        log.info("CoordinatorTest.apply gender to authors in papers()  resetTime = ${resetTime}")
        val res = paperRepo.getPapersWithAuthors(100)
        assertEquals(14, res.size)
    }


    //    @Test
    fun `build a raw journal table`() {
        coordinator.resetJournalTable()
        coordinator.buildJournalTable()
        val stats = stats.statsJournalTable()
        assertEquals(3157, stats.totalJournals)
    }

    //    @Test
    fun `should copy author to new table & assign gender`() {
        println("${Date()} dbLive.clearGenderedAuthorsTable()")
        mongo.clearGenderedAuthorsTable()
        println("${Date()} dbLive.resetIndexes()")
        mongo.resetIndexes()
        println("${Date()} repo.buildGenderedAuthorsTable()")
        coordinator.buildGenderedAuthorsTable(500000)
        println("${Date()} done -- should copy author to new table & assign gender")
    }


    //        @Test
    fun `extract authors from raw papers into raw author table`() {
        val resetTime = measureTimeMillis {
            coordinator.resetForBuildRawAuthorTable()
        }
        log.info("CoordinatorTest.extract authors from raw papers into author table()  resetTime = $resetTime")

        val parseTime = measureTimeMillis {
            val res = coordinator.runParseRawAuthorTableFromRawPapers()
            assertEquals(137438, res.size)
        }
        log.info("CoordinatorTest.extract authors from raw papers into author table()  parseTime = $parseTime")
    }

//    @Test
    fun `convert full csv file to authors`() {
//        val authors = CSVParser.csvFileToAuthors(samplePath)
//        assertTrue(authors.size > 80000)
//        println("done")
    }

    @Test
    fun `parse initial csv file into paper table`() {
        val clearTime = measureTimeMillis {
            paperRepo.clearRawPapers()
        }
        log.info("CoordinatorTest.parse initial csv file into paper table()  clearTime = $clearTime")

        val parseTime = measureTimeMillis {
            val res = coordinator.runParseCsvToRawPapers()
            assertEquals(27129, res.size)
        }
        log.info("CoordinatorTest.parse initial csv file into paper table()  parseTime = $parseTime")
    }
}
