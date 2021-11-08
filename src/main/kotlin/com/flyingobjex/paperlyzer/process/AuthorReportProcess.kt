package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.control.AuthorReportLine
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.ReportRepository
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlin.streams.asSequence
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

data class AuthorReportStats(
    val totalReportsProcessed: Long,
    val totalUnprocessed: Long,
) {
    override fun toString(): String {
        return ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "!!     Author Report Process      !!" +
            "!!     Author Report Process      !!" +
            "\n\n" +
            "totalReportsProcessed: $totalReportsProcessed \n" +
            "totalUnprocessed: $totalUnprocessed \n" +
            "PROCESSED_RECORDS_GOAL: $UNPROCESSED_RECORDS_GOAL \n" +
            "API_BATCH_SIZE: $API_BATCH_SIZE \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n"
    }
}

class AuthorReportProcess(val mongo:Mongo) : IProcess {

    val log: Logger = Logger.getAnonymousLogger()
    private val authorRepo = AuthorRepository(mongo)
    private val reportRepo = ReportRepository(mongo)

    override fun init() {
        log.info("AuthorReportProcess.init()  ")
    }

    override fun name(): String = "Author Report Process"

    override fun runProcess() {
//        val unprocessed = authorRepo.getUnprocessedAuthorsByReport(API_BATCH_SIZE)

        val querySize = 500000
        val batch: List<Author> = authorRepo.getGenderedAuthors(querySize)

        batch.parallelStream().asSequence().filterNotNull().forEach { author ->
            val years = author.toYearsPublished()
                .mapNotNull { it.toIntOrNull() }
                .sorted()

            reportRepo.addAuthorReportLine(
                AuthorReportLine(
                    author.firstName ?: "",
                    author.lastName,
                    author.gender.gender,
                    author.gender.probability,
                    author.paperCount,
                    years.joinToString(";"),
                    years.firstOrNull() ?: 0,
                    years.lastOrNull() ?: 0,
                    author.publishedTitles().joinToString(";"),
                    author.orcIDString,
                    author.averageCoAuthors ?: -9.9
                )
            )
        }
    }

    override fun shouldContinueProcess(): Boolean {
        return false
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val stats = reportRepo.getAuthorReportStats()
        log.info("AuthorReportProcess.printStats()  stats = \n $stats")
        GlobalScope.launch {
            outgoing?.send(Frame.Text(stats.toString()))
        }
        return stats.toString()
    }

    override fun cancelJobs() {}

    override fun reset() {
        authorRepo.resetAuthorReport()
        reportRepo.resetAuthorReport()
    }
}
