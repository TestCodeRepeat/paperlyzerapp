package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import io.ktor.http.cio.websocket.*
import kotlin.math.abs
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

data class SsAuthorProcessStats(
    val totalRawPapersProcessed: Int,
    val totalRawPapersUnprocessed: Int,
    val totalUnidentified: Int,
    val totalWosPapers: Int,
) {
    override fun toString(): String {
        return ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "!!     SsAuthorToRawPaperProcess Process      !!" +
            "!!     SsAuthorToRawPaperProcess Process      !!" +
            "\n\ntotalRawPapersProcessed: $totalRawPapersProcessed \n" +
            "totalRawPapersUnprocessed: $totalRawPapersUnprocessed \n" +
            "totalUnidentified: $totalUnidentified \n" +
            "totalWosPapers: $totalWosPapers \n" +
            "UNPROCESSED_RECORDS_GOAL: $UNPROCESSED_RECORDS_GOAL \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n"
    }
}

class SsAuthorToRawPaperProcess(val mongo: Mongo) : IProcess {

    val authorRepo = SemanticScholarAuthorRepo(mongo)

    override fun init() {
        println("SsAuthorToRawPaperProcess.kt :: init :: SsAuthorToRawPaperProcess init()")
    }

    override fun name(): String = "SsAuthorToRawPaperProcess"

    override fun runProcess() {
        val batchSize = API_BATCH_SIZE
        var unprocessed = emptyList<WosPaper>()
        val time = measureTimeMillis {
            unprocessed = authorRepo.getUnprocessedRawPapers(batchSize)
        }
        println("SsAuthorToRawPaperProcess.kt :: getUnprocessed() :: time = ${time}")
        unprocessed.parallelStream().forEach { wosPaper ->
            // get matching SsPaper via doi
            authorRepo.getSsPaperByWosDoi(wosPaper.doi)?.let { matchinSsPaper ->
                // apply SsAuthors to Raw Paper record
                val wosPaperAuthorCount = wosPaper.authors.size
                val ssPaperAuthorCount = matchinSsPaper.authors?.size ?: -5
                authorRepo.updatePaper(
                    wosPaper.copy(
                        ssAuthors = matchinSsPaper.authors,
                        ssAuthorProcessedStep1 = true,
                        wosPaperAuthorCount = wosPaperAuthorCount,
                        ssPaperAuthorCount = ssPaperAuthorCount,
                        authorCountDifference = abs(wosPaperAuthorCount - ssPaperAuthorCount)
                    )
                )
            } ?: run {
                authorRepo.updateRawPaperWithSsAuthor(wosPaper._id ?: "", emptyList())
            }
        }
    }

    override fun shouldContinueProcess(): Boolean {
        val res = authorRepo.getUnprocessedRawPapersCount()
        println("SsAuthorToRawPaperProcess.kt :: shouldContinueProcess() :: UnProcessed Records Count  = ${res}")
        return res > UNPROCESSED_RECORDS_GOAL
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val stats = authorRepo.getRawPaperWithSsAuthorStats().toString()
        println(
            "SsAuthorToRawPaperProcess.kt :: printStats() :: res = \n ${stats} \n" +
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
        println("SsAuthorToRawPaperProcess.reset()  !!!!!!!! RESET !!!!!!!!")
        authorRepo.resetSsAuthorDataStep1()
    }

    override fun type(): ProcessType = ProcessType.SsAuthor

}
