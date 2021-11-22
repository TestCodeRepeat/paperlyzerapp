package com.flyingobjex.paperlyzer.process.reports

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.control.StatsController
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.process.IProcess
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import org.litote.kmongo.ne

data class ReportStats(
    val totalReportsProcessed: Int,
    val totalUnprocessed: Int,
) {
    override fun toString(): String {
        return ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "!!     Report Process      !!" +
            "!!     Report Process      !!" +
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

class PaperReportProcess(
    val mongo: Mongo,
) : IProcess {

    val log: Logger = Logger.getAnonymousLogger()
    private val wosRepo = WoSPaperRepository(mongo)
    private val stats = StatsController(mongo)

    override fun reset() {
        log.info("ReportProcess.reset()  ")
        wosRepo.resetReportLines()
    }

    override fun type(): ProcessType = ProcessType.paperReport

    override fun runProcess() {
        log.info("ReportProcess.runProcess()  ")
        val unprocessed = stats.getUnprocessedPapersByReport(API_BATCH_SIZE)
        val asReportLines = stats.papersToReportLines(unprocessed)
        stats.addReports(asReportLines)
        stats.markAsReported(unprocessed)
    }

    override fun shouldContinueProcess(): Boolean =
        (mongo.genderedPapers.countDocuments(WosPaper::reported ne true) > UNPROCESSED_RECORDS_GOAL)


    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val stats = wosRepo.getReportStats()
        log.info("WosCitationProcess.printStats()  stats = \n $stats")
        GlobalScope.launch {
            outgoing?.send(Frame.Text(stats.toString()))
        }
        return stats.toString()
    }

    override fun cancelJobs() {
        log.info("ReportProcess.cancelJobs()  ")
    }

    override fun name(): String = "Report Process"

    override fun init() {
        log.info("ReportProcess.init()  ")
    }
}
