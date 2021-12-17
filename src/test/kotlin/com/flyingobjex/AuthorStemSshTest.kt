package com.flyingobjex

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.parser.DisciplineType
import com.flyingobjex.paperlyzer.process.AuthorStemSshProcess
import com.flyingobjex.paperlyzer.process.DisciplineUtils
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.usecase.StemSshUseCase
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.litote.kmongo.aggregate
import org.litote.kmongo.eq
import org.litote.kmongo.limit
import org.litote.kmongo.match

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
    private val stemSshUseCase = StemSshUseCase(mongo)

    private val authorStemProcess = AuthorStemSshProcess(mongo)
    val app = PaperlyzerApp(mongo)

    fun getStemSshPapers(): List<WosPaper> {
        val ssh = mongo.genderedPapers.aggregate<WosPaper>(
            match(WosPaper::discipline eq DisciplineType.SSH),
            limit(10)
        ).toList()
        val stem = mongo.genderedPapers.aggregate<WosPaper>(
            match(WosPaper::discipline eq DisciplineType.STEM),
            limit(10)
        ).toList()

        return ssh + stem
    }

    @Test
    fun `app should run process three times and stop`() {
        app.process?.printStats()
        app.process?.reset()
        app.process?.printStats()

        app.runProcess()

        mongo.genderedAuthors
            .countDocuments(Author::discipline eq DisciplineType.UNINITIALIZED) shouldBeLessThan 420000
    }


    @Test
    fun `should check for unprocessed before continuing`() {
        authorStemProcess.reset()
        authorStemProcess.runProcess()
        authorStemProcess.printStats()
        authorStemProcess.shouldContinueProcess() shouldBe true

        mongo.genderedAuthors
            .countDocuments(Author::discipline eq DisciplineType.UNINITIALIZED) shouldBe 423470L
    }

    @Test
    fun `should calculate a mix of SSH and STEM papers`() {

        val papers = getStemSshPapers()
        val a = papers.subList(0, 1)
        val b = papers.subList(10, 11)
        val res = a + b
        DisciplineUtils.calculateStemSshScores(res) shouldBe 0.5
    }

    //    @Test
    fun `should get unprocessed authors`() {
        authorStemProcess.reset()
        stemSshUseCase.getUnprocessedAuthorsByStemSsh(1).firstOrNull() shouldNotBe null
    }

    //    @Test
    fun `should reset author table for stem ssh`() {
        authorStemProcess.reset()
        mongo.genderedAuthors
            .countDocuments(Author::disciplineScore eq -5.5) shouldBe 423480L

    }


}
