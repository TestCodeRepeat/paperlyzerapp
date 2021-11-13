package com.flyingobjex

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.parser.DisciplineType
import com.flyingobjex.paperlyzer.process.AuthorStemSshProcess
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.litote.kmongo.eq


/**
 *
 *  Apply STEM/SSH/M to the author table.
I calculated the coding by defining
STEM = 0, M = .5, SSH = 1
Then I took the discipline from the papers of every
single author and took the mean value.
E.g. Jane Smith had 2 STEM and a M paper: 0 + 0 + .5 / 3 = .16
resulting in Jane Smith being STEM.
I defined the intervals as such: STEM = 0 to .45, M = .45 to .55, SSH = .55 to 1.
 *
 * */

class AuthorStemSshTest {

    val mongo = Mongo(true)
    private val authorRepo = AuthorRepository(mongo)
    private val paperRepo = WoSPaperRepository(mongo)

    private val authorStemProcess = AuthorStemSshProcess(mongo)
    val app = PaperlyzerApp(mongo)


    @Test
    fun `should check for unprocessed before continuing`(){
        authorStemProcess.reset()
        authorStemProcess.runProcess()
        authorStemProcess.printStats()
        authorStemProcess.shouldContinueProcess() shouldBe true

        mongo.genderedAuthors
            .countDocuments(Author::discipline eq DisciplineType.UNINITIALIZED) shouldBe 423470L
    }

    @Test
    fun `should get unprocessed authors`() {
        authorRepo.getUnprocessedAuthorsByStemSsh(1).firstOrNull() shouldNotBe null
    }

//    @Test
    fun `should reset author table for stem ssh`() {
        mongo.genderedAuthors
            .countDocuments(Author::discipline eq DisciplineType.UNINITIALIZED) shouldBe 423480L

    }


}
