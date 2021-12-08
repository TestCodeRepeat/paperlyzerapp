package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.SendChannel

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
            "!!     Discipline Process      !!" +
            "!!     Discipline Process      !!" +
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

    }

    override fun shouldContinueProcess(): Boolean {
        TODO("Not yet implemented")
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val res = authorRepo.getSsAuthorStats().toString()
        println(
            "SemanticScholarAuthorProcess.kt :: printStats() :: res = \n ${res} \n" +
                "========================================= \n"
        )
        return res
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
