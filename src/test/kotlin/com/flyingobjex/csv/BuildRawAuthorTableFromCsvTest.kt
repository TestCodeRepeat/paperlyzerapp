package com.flyingobjex.csv

import com.flyingobjex.paperlyzer.*
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentitiy
import com.flyingobjex.paperlyzer.parser.CSVParser
import com.flyingobjex.paperlyzer.repo.CsvParserRepo
import org.junit.Test
import org.litote.kmongo.div
import org.litote.kmongo.eq
import kotlin.test.assertTrue

class BuildRawAuthorTableFromCsvTest {

    private val testPath = "src/test/resources/author_table_test.csv"
    private val dbTest = Mongo()

    private val livePath = "../tbl_cli_full.tsv"
    private val dbLive = Mongo(true)

    @Test
    fun `parse live csv file into author live database table`(){
        dbLive.clearRawAuthors()
        dbLive.clearAuthors()
        val repo = CsvParserRepo(dbLive)
        val authors = repo.liveCsvFileToAuthorTable(livePath)
        assertTrue(authors.size > 40)
        val unassigned = dbLive.rawAuthors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED)
        val initialsOnly = dbLive.rawAuthors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.INITIALS)
        val undetermined = dbLive.rawAuthors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.NOFIRSTNAME)
        println("unassigned = $unassigned")
        println("initialsOnly = $initialsOnly")
        println("undetermined = $undetermined")
        println("done")
    }

//    @Test
    fun `should apply orchid building author table`(){
        println("done")
    }


    //
//
//    @Test
    fun `convert full csv file to authors`() {
        val authors = CSVParser.csvFileToAuthors(livePath)
        assertTrue(authors.size > 80000)
        println("done")
    }
//
//    @Test
//    fun `load test file into test database`() {
//        return
//        dbTest.clearAuthors()
//        val rows = DbUtil.testCsvFileToAuthorTable(testPath, dbTest)
////        val csvLines = CSVUtil.parse(rows)/**/
//        println("done")
//    }

//    @Test
//    fun `load partial csv file into database`() {
//        mongoTestDb.clearAuthors()
//        val authors = CSVUtil.parseCsvFileToDatabase(testPath, mongoTestDb)
//        assertTrue(authors.size > 50)
//    }

    //    @Test
//    fun `parse test csv file into author test database table`(){
//        dbTest.clearRawAuthors()
//        val repo = Repository(dbTest)
//        val authors = repo.testCsvFileToAuthorTable(testPath)
//        assertTrue(authors.size > 40)
//        println("done")
//    }
}
