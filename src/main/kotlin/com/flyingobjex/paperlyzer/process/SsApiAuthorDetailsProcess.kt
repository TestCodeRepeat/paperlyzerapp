package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import io.ktor.http.cio.websocket.*
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.channels.SendChannel

data class SsApiAuthorDetailsStats(
    val totalRawPapersProcessed: Int,
    val totalRawPapersUnprocessed: Int,
    val totalUnidentified: Int,
    val totalWosPapers: Int,
    val totalSsAuthorsFound: Int,
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


class SsApiAuthorDetailsProcess(val mongo: Mongo) :IProcess {

    val authorRepo = SemanticScholarAuthorRepo(mongo)

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


    }

    override fun shouldContinueProcess(): Boolean {
        TODO("Not yet implemented")
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        TODO("Not yet implemented")
    }

    override fun cancelJobs() {
        TODO("Not yet implemented")
    }

    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun type(): ProcessType = ProcessType.SsApiAuthor
}
