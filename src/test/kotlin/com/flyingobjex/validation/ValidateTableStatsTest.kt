package com.flyingobjex.validation

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.MainCoordinator
import com.flyingobjex.paperlyzer.control.StatsController
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.usecase.GenderedAuthorUseCase
import org.junit.Test
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class ValidateTableStatsTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)
    private val samplePath = "../tbl_cli_sample.tsv"
    private val coordinator = MainCoordinator(mongo, samplePath)
    private val paperRepo = WoSPaperRepository(mongo)
    private val stats = StatsController(mongo)
    private val genderedAuthorUseCase = GenderedAuthorUseCase(mongo)

    @Test
    fun `should have stats on Journals table`() {
        val queryTime = measureTimeMillis {
            val res = stats.statsJournalTable()
            print(res)
        }
        log.info("ValidateTableStatsTest.should have stats on Journals table()  queryTime = ${queryTime}")
    }

    @Test
    fun `should have stats on GenderedAuthors table`() {
        val queryTime = measureTimeMillis {

            val res = genderedAuthorUseCase.statsGenderedAuthorsTable()
            assertEquals(405336, res.totalAuthors)
            assertEquals(467, res.totalWithNoAssignedGender)
        }
        log.info("ValidateTableStatsTest.should have stats on GenderedAuthors table()  queryTime = ${queryTime}")
    }

    @Test
    fun `check raw author's table has full range of published years`() {
        val queryTime = measureTimeMillis {
            stats.statsAuthorTable()
        }
        log.info("CoordinatorTest.check raw author's table has full range of published years()  queryTime = $queryTime")

    }
}
