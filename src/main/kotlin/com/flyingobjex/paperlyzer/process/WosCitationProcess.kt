package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.repo.SemanticScholarPaperRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

data class WosCitationStats(
    val totalProcessedWosPapers: Int,
    val totalUnprocessedWosPapers: Int,
    val totalFailedWosPapers: Int,
    val totalWosPapers: Int,
    val totalSsPapers: Int,
) {
    override fun toString(): String {
        return ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "!!     Citation Process      !!" +
            "!!     Citation Process      !!" +
            "\n\ntotalProcessedWosPapers: $totalProcessedWosPapers \n" +
            "totalUnprocessedWosPapers: $totalUnprocessedWosPapers \n" +
            "totalFailedWosPapers: $totalFailedWosPapers \n" +
            "totalWosPapers: $totalWosPapers \n" +
            "totalSsPapers: $totalSsPapers \n\n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n"
    }
}

class WosCitationProcess(val mongo: Mongo, logMessage: (value: String) -> Unit) : IProcess {

    val log: Logger = Logger.getAnonymousLogger()
    private val wosRepo = WoSPaperRepository(mongo)
    private val ssPaperRepo = SemanticScholarPaperRepository(mongo)

    override fun runProcess() {
        val batchSize = API_BATCH_SIZE
        log.info("WosCitationProcess.runProcess()  0000 :: batchSize = $batchSize")
        val wosUnprocessedPapers = wosRepo.getUnprocessedByCitations(batchSize)
        log.info("WosCitationProcess.runProcess()  1111")
        wosUnprocessedPapers.parallelStream().forEach { wosPaper ->
            ssPaperRepo.paperByDoi(wosPaper.doi)?.let { matchingPaper ->
                wosRepo.updateCitationsCount(wosPaper, matchingPaper.numCitedBy ?: 0, matchingPaper.influentialCitationCount ?: 0)
            } ?: run {
                wosRepo.updateCitationsCount(wosPaper, -5, -5)
            }
        }
        log.info("WosCitationProcess.runProcess()  2222 //////")
    }

    override fun shouldContinueProcess(): Boolean {
        val count = wosRepo.unprocessedCitationsCount()
        log.info("WosCitationProcess.shouldContinueProcess()  count/Goal = $count / $UNPROCESSED_RECORDS_GOAL")
        return count > UNPROCESSED_RECORDS_GOAL
    }

    override fun cancelJobs() {
        log.info("WosCitationProcess.cancelJobs()  ")
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val stats = wosRepo.getCitationStats()
        log.info("WosCitationProcess.printStats()  stats = ${stats}" )
        GlobalScope.launch {
            outgoing?.send(Frame.Text(stats.toString()))
        }
        return stats.toString()

    }

    override fun reset() {
        val res = wosRepo.resetCitationProcessed()
        log.info("WosCitationProcess.reset() :: completed = ${res}" )
    }

    override fun type(): ProcessType = ProcessType.Citation

    override fun name(): String {
        return "Wos Citation Process"
    }

    override fun init() {

    }
}
