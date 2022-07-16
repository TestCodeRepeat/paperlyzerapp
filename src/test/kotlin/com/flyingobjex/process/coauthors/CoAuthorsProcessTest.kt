package com.flyingobjex.process.coauthors

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.process.CoAuthorProcess
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

    private val process = CoAuthorProcess(mongo)
    private val app = PaperlyzerApp(mongo, process)


    @Test
    fun `app should run coauthor proces`() {
        verifyProcessType(app.process.type(), ProcessType.CoAuthor)
        app.process.printStats()
        app.process.reset()
        app.process.printStats()
        app.start()
        app.process.printStats()
    }

    //    @Test
    fun `should reset and print stats for coauthor process`() {
        process.reset()
        process.printStats()
    }

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
