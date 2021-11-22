package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.control.SSPaperController
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.SendChannel

const val NUM_GROUPS = 50

/**
 * Process for loading a Semantic Scholar paper for each
 * corresponding Web of Science paper
 * using the Semantic Scholar API
 * */
class WosToSsProcess(val mongo: Mongo, val logMessage: (value: String) -> Unit) : IProcess {

    private val ssController = SSPaperController(mongo, logMessage)

    var processedWosPaperGoal: Int = 0

    override fun runProcess() {
        ssController.processWosBatchGrouped(API_BATCH_SIZE, NUM_GROUPS)
    }

    override fun shouldContinueProcess(): Boolean {
        val processedCount = ssController.getProcessedWosPaperCount()
        val res = processedCount >= processedWosPaperGoal
        println("Should Start Process: $res")
        println("PaperlyzerApp.kt :: processedCount = $processedCount")
        println("PaperlyzerApp.kt :: shouldStartWosProcess() :: processedWosPaperGoal = " + processedWosPaperGoal)
        return res
    }

    override fun cancelJobs() {
        ssController.cancelJobs()
        println("WosToSsProcess.kt :: cancelJobs :: !!! CANCELED !!!")
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val stats = ssController.getStats().toString()
        logMessage(stats)
        println("WosToSsProcess.kt :: printStats() :: stats = $stats")
        return stats
    }

    override fun reset() {
        ssController.reset()
        println("WosToSsProcess.kt :: reset :: ")
    }

    override fun type(): ProcessType = ProcessType.sjr

    override fun name(): String {
        return "Web of Science to Semantic Scholar Process"
    }

    override fun init() {
        println("WosToSsProcess.kt :: init :: ")
    }
}


/** Configuration originally used when running in the cloud  */
const val API_BATCH_SIZE_WOS_PROCESS = 10
const val NUM_GROUPS_WOS_PROCSS = 50
