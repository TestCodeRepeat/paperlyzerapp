package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.api.SEMANTIC_SCHOLAR_API_KEY
import com.flyingobjex.paperlyzer.api.SemanticScholarAPI
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import io.ktor.http.cio.websocket.*
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class SsApiAuthorDetailsStats(
    val totalRawPapersProcessed: Int,
    val totalRawPapersUnprocessed: Int,
    val totalSsAuthorsFound: Int,
    val totalUnidentified: Int,
    val totalWosPapers: Int,
) {
    override fun toString(): String {
        return ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "!!     SsApiAuthorDetailsStats Process      !!" +
            "!!     SsApiAuthorDetailsStats Process      !!" +
            "\n\ntotalRawPapersProcessed: $totalRawPapersProcessed \n" +
            "totalRawPapersUnprocessed: $totalRawPapersUnprocessed \n" +
            "totalUnidentified: $totalUnidentified \n" +
            "totalWosPapers: $totalWosPapers \n" +
            "totalSsAuthorsFound: $totalSsAuthorsFound \n" +
            "UNPROCESSED_RECORDS_GOAL: $UNPROCESSED_RECORDS_GOAL \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n"
    }
}


class SsApiAuthorDetailsProcess(val mongo: Mongo) : IProcess {

    val authorRepo = SemanticScholarAuthorRepo(mongo)
    val api = SemanticScholarAPI(SEMANTIC_SCHOLAR_API_KEY)

    override fun init() {
        println("SsApiAuthorDetailsProcess.kt :: init :: ")
    }

    override fun name(): String = "SsApiAuthorDetailsProcess"

    override fun runProcess() {
        // fetch all authors for ss papers
        val batchSize = API_BATCH_SIZE
        var unprocessed = emptyList<WosPaper>()
        val time = measureTimeMillis {
            unprocessed = authorRepo.getUnprocessedRawPapersBySsAuthorDetails(batchSize)
        }

        println("SsApiAuthorDetailsProcess.kt :: load unprocessed :: time = $time")

        unprocessed.parallelStream().forEach { wosPaper ->
            println("SsApiAuthorDetailsProcess.kt :: runProcess :: 0000")
            wosPaper.ssAuthors?.parallelStream()?.forEach { ssAuthorData ->
                ssAuthorData.authorId?.let { authorId ->
                    runBlocking {
                        launch(IO) {
                            api.authorById(authorId)?.let { ssAuthorDetails ->
                                println("SsApiAuthorDetailsProcess.kt :: runProcess() :: ssAuthorDetails.authorId = ${ssAuthorDetails.authorId}")
                                authorRepo.addSsAuthorDetails(ssAuthorDetails)
                            }
                        }
                    }
                }
            }

            println("SsApiAuthorDetailsProcess.kt :: runProcess :: 1111")
            authorRepo.updateRawPaperWithSsAuthorStep2(wosPaper._id)
        }
    }

    override fun shouldContinueProcess(): Boolean {
        val res = authorRepo.getUnprocessedRawPapersBySsAuthorDetailsCount()
        println("SsApiAuthorDetailsProcess.kt :: shouldContinueProcess() :: res = $res")
        return res > UNPROCESSED_RECORDS_GOAL

    }

    override fun printStats(outgoing: SendChannel<Frame>?): String =
        authorRepo.getSsApiAuthorDetailsStats().toString()


    override fun cancelJobs() {
        TODO("Not yet implemented")
    }

    override fun reset() {
        println("SsApiAuthorDetailsProcess.kt :: reset :: ")
        authorRepo.resetSsAuthorDataStep2()
    }

    override fun type(): ProcessType = ProcessType.SsApiAuthor
}
