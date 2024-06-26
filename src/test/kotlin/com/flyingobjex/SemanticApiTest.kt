package com.flyingobjex

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.api.SEMANTIC_SCHOLAR_API_KEY
import com.flyingobjex.paperlyzer.api.SemanticScholarAPI
import com.flyingobjex.paperlyzer.repo.SemanticScholarPaperRepository
import io.kotest.matchers.comparables.shouldBeGreaterThan
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun logMessage(message: String) {}

class SemanticApiTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)
    private val ssPaperRepo = SemanticScholarPaperRepository(mongo)
    private val api = SemanticScholarAPI(SEMANTIC_SCHOLAR_API_KEY)
    private val doi = "10.1016/j.apenergy.2011.12.064"
    private val authorId = "145631702"


    @Test
    fun `API should load author data by id`(){
        runBlocking {
            api.authorById(authorId)?.let { ssAuthor ->
                ssAuthor.aliases!!.size shouldBeGreaterThan 0
                ssAuthor.papers!!.size shouldBeGreaterThan 0
            } ?: run {
                assertTrue(false, "failed to get result from API")
            }
        }
    }

//    @Test
    fun `API test - should get authors for doi of paper `() =
        runBlocking {
            api.fetchSsPaperByDoi(doi)?.let { pap ->
                ssPaperRepo.insertPaper(pap)
                val res = ssPaperRepo.getAllSsPapers()
                assertEquals(res.first().paperId, pap.paperId)
            } ?: run {
                assertTrue(false, "failed to get result from API")
            }
        }
}
