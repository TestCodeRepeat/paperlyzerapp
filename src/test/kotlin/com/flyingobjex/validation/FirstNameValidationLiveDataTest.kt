package com.flyingobjex.validation

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.MainCoordinator
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.FirstName
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import org.litote.kmongo.regex
import java.util.logging.Logger
import kotlin.test.assertEquals

class FirstNameValidationLiveDataTest {
    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(true)
    private val samplePath = "../tbl_cli_full.tsv"
    private val coordinator = MainCoordinator(mongo, samplePath)
    private val paperRepo = WoSPaperRepository(mongo)
    private val authorRepo = AuthorRepository(mongo)

//    @Test
    fun `count hyphenated first names in firstNames table`() {
        val resA = authorRepo.mongo.firstNameTable.find(
            FirstName::firstName regex ".*-.*"
        ).toList()
        assertEquals(14515, resA.size)

        val res = authorRepo.mongo.firstNameTable.find(
            FirstName::originalFirstName regex ".*-.*"
        ).toList()
        assertEquals(14515, res.size)
    }

//    @Test
    fun `count from hypenated names in Authors table`() {
        val res = authorRepo.mongo.authors.find(
            Author::firstName regex ".*-.*"
        ).toList()
        assertEquals(21345, res.size)
    }

}
