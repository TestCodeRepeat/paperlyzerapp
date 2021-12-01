package com.flyingobjex.paperlyzer.process.reports

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.parser.DisciplineType
import com.flyingobjex.paperlyzer.process.IProcess
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.ReportRepository
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlin.streams.asSequence
import kotlin.system.measureTimeMillis
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

class AuthorReportProcess(val mongo: Mongo) : IProcess {

    val log: Logger = Logger.getAnonymousLogger()

    private val authorRepo = AuthorRepository(mongo)
    private val reportRepo = ReportRepository(mongo)

    override fun init() {
        log.info("AuthorReportProcess.init()  ")
    }

    override fun name(): String = "Author Report Process"

    override fun runProcess() {
        val unprocessed = authorRepo.getUnprocessedAuthorsByAuthorReport(API_BATCH_SIZE)

        unprocessed.parallelStream().asSequence().forEach { author ->
            val years = author.toYearsPublished()
                .mapNotNull { it.toIntOrNull() }
                .sorted()

            reportRepo.addAuthorReportLine(
                AuthorReportLine(
                    firstName = author.firstName ?: "",
                    lastName = author.lastName,
                    gender = author.gender.gender,
                    genderProbability = author.gender.probability,
                    totalPapers = author.paperCount,
                    totalAsFirstAuthor = author.firstAuthorCount?.toInt() ?: 0,
                    yearsPublished = years.joinToString(";"),
                    firstYearPublished = years.firstOrNull() ?: 0,
                    lastYearPublished = years.lastOrNull() ?: 0,
                    publishedShortTitles = author.publishedShortTitles().joinToString(";"),
                    orcID = author.orcIDString,
                    coAuthorMean = author.averageCoAuthors ?: -9.9,
                    discipline = author.discipline ?: DisciplineType.NA,
                    disciplineScore = author.disciplineScore ?: -5.5,
                    sjrScores = "",
                    hIndexes = "",
                )
            )

            authorRepo.updateAuthorUnprocessedForAuthorReport(author)
        }
    }

    override fun shouldContinueProcess(): Boolean {
        var shouldContinue: Boolean
        val time = measureTimeMillis {
            val unprocessedCount = authorRepo.unprocessedAuthorsByAuthorReportCount()
            log.info("AuthorReportProcess.shouldContinueProcess()  unprocessedCount = $unprocessedCount")
            shouldContinue = unprocessedCount > UNPROCESSED_RECORDS_GOAL
        }
        log.info("AuthorReportProcess.shouldContinueProcess()  time = $time ms")
        return shouldContinue
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
        log.info("AuthorReportProcess.reset()  process = ${name()}" )
        authorRepo.resetAuthorReport()
        reportRepo.resetAuthorReport()
    }

    override fun type(): ProcessType = ProcessType.authorReport
}
