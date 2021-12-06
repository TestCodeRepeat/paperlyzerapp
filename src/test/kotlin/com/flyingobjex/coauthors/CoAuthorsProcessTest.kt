package com.flyingobjex.coauthors

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.process.CoAuthorProcess
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.util.GenderUtils.allPapersAreGenderComplete
import com.flyingobjex.paperlyzer.util.GenderUtils.averageGenderRatio
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.litote.kmongo.aggregate
import org.litote.kmongo.limit
import org.litote.kmongo.match
import org.litote.kmongo.ne

fun verifyProcessType(appProcessType: ProcessType, type: ProcessType) {
    if (appProcessType != type) throw Error("Wrong Process!!! : ${appProcessType} should be $type")
}

class CoAuthorsProcessTest {
    private val mongo = Mongo(false)
    private val wosRepo = WoSPaperRepository(mongo)
    private val authorRepo = AuthorRepository(mongo)


    private val process = CoAuthorProcess(mongo)
    private val app = PaperlyzerApp(mongo)


//    @Test
    fun `should calculate average gender ratio of papers`() {
        val papers = mongo.genderedPapers.aggregate<WosPaper>(
            match(WosPaper::authors ne null),
            limit(50)
        ).toList()
            .filter { it.genderCompletenessScore == 1.0 }

        allPapersAreGenderComplete(papers) shouldBe true

        val res = averageGenderRatio(papers)
        res!! shouldBeGreaterThan 0.0
        res shouldBeLessThan 1.0
    }

        @Test
    fun `app should run coauthor proces`() {
        val processType = app.process.type()
        verifyProcessType(processType, ProcessType.coauthor)
        app.process.printStats()
        app.process.reset()
        app.process.printStats()
        app.start()
        app.process.printStats()
    }

    //    @Test
    fun `should run coauthor process`() {
        process.printStats()
        process.runProcess()
        process.printStats()
    }

    //    @Test
    fun `should reset co authors`() {
        process.printStats()
        process.reset()
        process.printStats()
    }

}
