package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch


data class SJRStats(
    val totalProcessedWithSJRIndex: Int,
    val totalWithMatchingSJR: Int,
    val totalUnidentified: Int,
    val totalUnprocessed: Int,
    val totalWosPapers: Int
){
    override fun toString(): String {
        return ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "!!     SJR/H Index Process      !!" +
            "!!     SJR/H Index Process      !!" +
            "\n\ntotalProcessedWithSJRIndex: $totalProcessedWithSJRIndex \n" +
            "totalUnprocessed: $totalUnprocessed \n" +
            "totalWithMatchingSJR: $totalWithMatchingSJR \n" +
            "totalUnidentified: $totalUnidentified \n" +
            "totalWosPapers: $totalWosPapers \n" +
            "UNPROCESSED_RECORDS_GOAL: $UNPROCESSED_RECORDS_GOAL \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n"
    }
}

class SJRProcess(val mongo: Mongo) : IProcess {

    val log: Logger = Logger.getAnonymousLogger()
    private val wosRepo = WoSPaperRepository(mongo)

    override fun init() {
        println("SJRProcess.kt :: init :: ")
    }

    override fun name(): String = "SJR / H Index Process"

    override fun shouldContinueProcess(): Boolean {
        var shouldContinue = false
        val time = measureTimeMillis {
            shouldContinue = wosRepo.unprocessedSJRIndexCount() > UNPROCESSED_RECORDS_GOAL
        }
        println("DisciplineProcess.shouldContinue() time == $time")
        return shouldContinue
    }

    override fun runProcess() {
        TODO("Not yet implemented")
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val stats = wosRepo.getSJRStats()
        log.info("WosCitationProcess.printStats()  stats = ${stats}")
        GlobalScope.launch {
            outgoing?.send(Frame.Text(stats.toString()))
        }
        return stats.toString()
    }

    override fun cancelJobs() {
//        TODO("Not yet implemented")
    }

    override fun reset() = wosRepo.resetSJRIndexProcessed()
}
