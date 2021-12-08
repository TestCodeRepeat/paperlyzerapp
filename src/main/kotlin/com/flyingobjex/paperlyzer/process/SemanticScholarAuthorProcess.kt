package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import io.ktor.http.cio.websocket.*
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

data class SsAuthorProcessStats(
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
            "!!     SemanticScholarAuthorProcess Process      !!" +
            "!!     SemanticScholarAuthorProcess Process      !!" +
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

class SemanticScholarAuthorProcess(val mongo: Mongo) : IProcess {

    val authorRepo = SemanticScholarAuthorRepo(mongo)

    override fun init() {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        TODO("Not yet implemented")
    }

    override fun runProcess() {
        val batchSize = API_BATCH_SIZE
        var unprocessed = emptyList<WosPaper>()
        val time = measureTimeMillis {
            unprocessed = authorRepo.getUnprocessedRawPapers(batchSize)

        }

        unprocessed.parallelStream().forEach { wosPaper ->
            // get matching SsPaper
            authorRepo.getSsPaperByWosDoi(wosPaper.doi)?.let { matchinSsPaper ->
                // apply SsAuthors to Raw Paper record
                authorRepo.upadteRawPaperWithSsAuthor(wosPaper._id ?: "", matchinSsPaper.authors ?: emptyList())
                val wosPaperAuthorCount = wosPaper.authors.size
                val ssPaperAuthorCount = matchinSsPaper.authors?.size ?: 0
                if (wosPaperAuthorCount != ssPaperAuthorCount) {
                    println(
                        "SemanticScholarAuthorProcess.kt ::\n " +
                            "!! Author Counts Not Equal !!  " +
                            "wosPaperAuthorCount = ${wosPaperAuthorCount}" +
                            "ssPaperAuthorCount = ${ssPaperAuthorCount}" +
                            ""
                    )
                }
            } ?: run {
                authorRepo.upadteRawPaperWithSsAuthor(wosPaper._id ?: "", emptyList())
            }
        }

    }

    override fun shouldContinueProcess(): Boolean {
        val res = authorRepo.getUnprocessedRawPapersCount()
        println("SemanticScholarAuthorProcess.kt :: shouldContinueProcess() :: UnProcessed Records Count  = ${res}")
        return res > UNPROCESSED_RECORDS_GOAL
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val stats = authorRepo.getSsAuthorStats().toString()
        println(
            "SemanticScholarAuthorProcess.kt :: printStats() :: res = \n ${stats} \n" +
                "========================================= \n"
        )
        GlobalScope.launch {
            outgoing?.send(Frame.Text(stats))
        }
        return stats
    }


    override fun cancelJobs() {
        TODO("Not yet implemented")
    }

    override fun reset() {
        authorRepo.resetSsAuthorData()
    }

    override fun type(): ProcessType {
        TODO("Not yet implemented")
    }

}
