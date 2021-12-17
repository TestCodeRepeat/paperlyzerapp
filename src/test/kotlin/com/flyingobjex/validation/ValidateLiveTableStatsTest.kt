package com.flyingobjex.validation

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.Stats
import com.flyingobjex.paperlyzer.usecase.GenderedAuthorUseCase
import org.junit.Test
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class ValidateLiveTableStatsTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(true)
    private val samplePath = "../tbl_cli_full.tsv"
    private val stats = Stats(mongo)
    private val genderedAuthorUseCase = GenderedAuthorUseCase(mongo)

//    @Test
    fun `should have stats on Journals table`(){
        val stats = stats.statsJournalTable()
        stats.top50Journals.forEach {
            println("${it.name} :: ${it.numOfCitations}")
        }
        assertEquals(22273, stats.totalJournals)
    }

    @Test
    fun `should have stats on GenderedAuthors table`()
    {
        val res = genderedAuthorUseCase.statsGenderedAuthorsTable()
        assertEquals(405336, res.totalAuthors)
        assertEquals(1855, res.totalWithNoAssignedGender)
    }

    @Test
    fun `check raw author's table has full range of published years (sanity check)`() {
        val queryTime = measureTimeMillis {
            val res = stats.authorYearsPublishedStats()
            assertEquals(58, res.yearsPublishedCountByYear?.size)
        }
        log.info("CoordinatorTest.check raw author's table has full range of published years()  queryTime = $queryTime")
    }
}
