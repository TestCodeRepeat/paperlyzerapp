package com.flyingobjex.tableBuilders

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.MainCoordinator
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentitiy
import com.flyingobjex.paperlyzer.entity.hasTwoDots
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import org.junit.Test
import org.litote.kmongo.div
import org.litote.kmongo.eq
import java.util.*
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

    //        @Test
    fun `build author table from raw author table`() {
        val resetTime = measureTimeMillis {
            coordinator.resetForAuthorTable()
        }
        log.info("CoordinatorTest.extract authors from raw papers into author table()  resetTime = $resetTime")
        val parseTime = measureTimeMillis {
            authorRepo.buildAuthorTableInParallel(10000)
        }
        log.info("CoordinatorTest.build author table from raw author table()  parseTime = $parseTime")

        val res = authorRepo.getAuhtorTableStats()
        assertEquals(9311, res)
    }

    //    @Test
    fun `should build author table from raw authors`() {
        val batchSize = 2000000
        println("${Date()} clearAuthors()")
        mongo.clearAuthors()
        println("${Date()} resetRawAuthors()")
        authorRepo.resetRawAuthors()
        println("${Date()} authorTableRepo.buildAuthorTableInParallel()")
        mongo.resetIndexes()
        println("${Date()} dbLive.resetIndexes()")
        authorRepo.buildAuthorTableInParallel(batchSize)
        println("${Date()} :: done ")
        val totalAuthors = mongo.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED)
        println("totalAuthors = $totalAuthors")

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
