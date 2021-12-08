package com.flyingobjex.util

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.SemanticScholarPaper
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import org.junit.Test
import org.litote.kmongo.*

class DatabaseMaintenanceTest {

    val mongo = Mongo()
    val authorRepo = SemanticScholarAuthorRepo(mongo)

//    @Test
    fun `apply uppercase to all Semantic Scholar doi's`() {
        println("DatabaseMaintenanceTest.kt :: apply uppercase to all Semantic Scholar doi's :: 1111")
        mongo.ssPapers.aggregate<SemanticScholarPaper>(
            match(SemanticScholarPaper::_id ne null),
        )

        mongo.ssPapers.find().toList().parallelStream().forEach { ssPaper ->
        println("DatabaseMaintenanceTest.kt :: apply uppercase to all Semantic Scholar doi's :: 2222")
            mongo.ssPapers.updateOne(
                SemanticScholarPaper::_id eq ssPaper._id,
                setValue(
                    SemanticScholarPaper::doi, ssPaper.doi?.uppercase()
                )
            )
        }
        println("DatabaseMaintenanceTest.kt :: apply uppercase to all Semantic Scholar doi's :: 3333")
    }

}
