package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.parser.TopicMatcher
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

data class DisciplineProcessStats(
    val totalProcessedWithDiscipline: Int,
    val totalUnprocessed: Int,
    val totalStem: Int,
    val totalSSH: Int,
    val totalMaybe: Int,
    val totalUnidentified: Int,
    val totalWosPapers: Int,
) {
    override fun toString(): String {
        return ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "!!     Discipline Process      !!" +
            "!!     Discipline Process      !!" +
            "\n\ntotalProcessedWithDiscipline: $totalProcessedWithDiscipline \n" +
            "totalUnprocessed: $totalUnprocessed \n" +
            "totalStem: $totalStem \n" +
            "totalSSH: $totalSSH \n" +
            "totalMaybe: $totalMaybe \n" +
            "totalUnidentified: $totalUnidentified \n" +
            "PROCESSED_RECORDS_GOAL: $UNPROCESSED_RECORDS_GOAL \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n"
    }
}

/**
 * Optimal API Batch Size: 10000
 *
 * */
class DisciplineProcess(
    val mongo: Mongo,
    private val matcher: TopicMatcher,
    val logMessage: ((value: String) -> Unit)? = null
) :
    IProcess {

    val log: Logger = Logger.getAnonymousLogger()
    private val wosRepo = WoSPaperRepository(mongo)

    override fun name(): String = "Discipline / Topic Process"

    override fun init() {
        println("DisciplineProcess.kt :: init :: ")

    }

    override fun shouldContinueProcess(): Boolean {
        var shouldContinue = false
        val time = measureTimeMillis {
            shouldContinue = wosRepo.unprocessedDisciplinesCount() > UNPROCESSED_RECORDS_GOAL
        }
        println("DisciplineProcess.shouldContinue() time == $time")
        return shouldContinue
    }


    override fun runProcess() {
        val batchSize = API_BATCH_SIZE
        log.info("WosCitationProcess.runProcess()  0000 :: batchSize = $batchSize")
        var unprocessed = emptyList<WosPaper>()
        val time = measureTimeMillis {
            unprocessed = wosRepo.getUnprocessedPapersByDiscipline(batchSize)
        }

        println("getUnprocessedPapersByDiscipline($batchSize):  $time ms")

        unprocessed.parallelStream().forEach { paper ->
            val matchingCriteriaForTopics = matcher.criteriaForTopics(paper.topics)
            val updated = wosRepo.applyMatchingCriteria(paper, matchingCriteriaForTopics)
            wosRepo.updatePaperDiscipline(updated)
        }

    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val stats = wosRepo.getDisciplineStats()
        log.info("WosCitationProcess.printStats()  stats = ${stats}")
        GlobalScope.launch {
            outgoing?.send(Frame.Text(stats.toString()))
        }
        return stats.toString()
    }

    override fun reset() = wosRepo.resetDisciplineProcessed()


    override fun cancelJobs() {
        println("DisciplineProcess.kt :: cancelJobs :: ")

    }
}
