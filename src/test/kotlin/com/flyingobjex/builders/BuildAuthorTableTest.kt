package com.flyingobjex.builders

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentitiy
import com.flyingobjex.paperlyzer.entity.hasTwoDots
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import org.junit.Test
import org.litote.kmongo.div
import org.litote.kmongo.eq
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildAuthorTableTest {

    private val dbLive = Mongo(true)
    private val authorTableRepo = AuthorRepository(dbLive)

//    @Test
    fun `should build author table from raw authors`(){
        val batchSize = 100000
        println("${Date()} clearAuthors()")
        dbLive.clearAuthors()
        println("${Date()} clearOrcidDuplicates()")
        dbLive.clearOrcidDuplicates()
        println("${Date()} resetRawAuthors()")
        dbLive.resetRawAuthors()
        println("${Date()} authorTableRepo.buildAuthorTableInParallel()")
        dbLive.resetIndexes()
        println("${Date()} dbLive.resetIndexes()")
        authorTableRepo.buildAuthorTableInParallel(batchSize)
        println("${Date()} :: done ")
        val totalAuthors = dbLive.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED)
        println("totalAuthors = $totalAuthors")
    }


    @Test
    fun `count number of potentially assignable genders`() {
        val unassigned = dbLive.rawAuthors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED)
        val initialsOnly = dbLive.rawAuthors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.INITIALS)
        val undetermined = dbLive.rawAuthors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.NOFIRSTNAME)
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