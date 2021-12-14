package com.flyingobjex.tableBuilders

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.MainCoordinator
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentitiy
import com.flyingobjex.paperlyzer.entity.hasTwoDots
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.litote.kmongo.div
import org.litote.kmongo.eq
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildAuthorTableTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(true)
    private val authorRepo = AuthorRepository(mongo)
    private val samplePath = "../tbl_cli_sample.tsv"
    private val coordinator = MainCoordinator(mongo, samplePath)

    @Test
    fun `get author table stats`() {
        val stats = authorRepo.getAuthorTableStats()
        log.info("BuildAuthorTableTest.get author table stats()  stats = ${stats}" )
        stats.totalAuthors shouldBe 562110
    }

    //    @Test
    fun `reset author table`() {
        log.info("BuildAuthorTableTest.reset author table()  START")
        val resetTime = measureTimeMillis {
            coordinator.resetForAuthorTable()
        }

        log.info("BuildAuthorTableTest.reset author table()  resetTime = ${resetTime}")
        log.info("BuildAuthorTableTest.reset author table()   authorRepo.getAuthorTableStats() = ${authorRepo.getAuthorTableStats()}")
    }

    //    @Test
    fun `build author table from raw author table`() {
        val resetTime = measureTimeMillis {
            coordinator.resetForAuthorTable()
        }
        log.info("CoordinatorTest.extract authors from raw papers into author table()  resetTime = $resetTime")
        val parseTime = measureTimeMillis {
            authorRepo.buildAuthorTableInParallel(2000000)
        }
        log.info("CoordinatorTest.build author table from raw author table()  parseTime = $parseTime")

        val res = authorRepo.getAuthorTableStats()
        assertEquals(444881, res.totalAuthors)
        // different results for
    }


    //    @Test
    fun `count number of potentially assignable genders`() {
        val unassigned = mongo.rawAuthors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED)
        val initialsOnly = mongo.rawAuthors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.INITIALS)
        val undetermined =
            mongo.rawAuthors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.NOFIRSTNAME)
        println("initialsOnly = $initialsOnly")
        println("undetermined = $undetermined")
        println()
    }

    //    @Test
    fun `should count unassigned raw authors`() {

    }

    @Test
    fun `string has two dots`() {
        assertTrue(hasTwoDots("L. M."))
        assertFalse(hasTwoDots("Sammuel L."))
    }

    //    @Test
    fun `should fail`() {
        assertTrue(hasTwoDots("L. Somebody"))
    }
}
