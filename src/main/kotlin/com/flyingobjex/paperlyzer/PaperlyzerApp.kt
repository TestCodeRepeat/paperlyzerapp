package com.flyingobjex.paperlyzer

import com.flyingobjex.paperlyzer.control.StatsController
import com.flyingobjex.paperlyzer.domain.topics
import com.flyingobjex.paperlyzer.parser.TopicMatcher
import com.flyingobjex.paperlyzer.process.*
import com.flyingobjex.plugins.SocketAction
import io.ktor.http.cio.websocket.*
import java.io.File
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

val API_BATCH_SIZE = System.getenv("API_BATCH_SIZE").toString().toInt()
val UNPROCESSED_RECORDS_GOAL = System.getenv("UNPROCESSED_RECORDS_GOAL").toString().toInt()

enum class ProcessType {
    citation, discipline, wostoss, report
}

const val BUILD_VERSION = 2
class PaperlyzerApp(val mongo: Mongo) {

    var serverUrl: String = "na"

    val log: Logger = Logger.getAnonymousLogger()

    var matcher = TopicMatcher(topics)

    lateinit var process: IProcess

    /** batchSize: # of concurrent API calls */
    var numConcurrentApiCalls: Int = API_BATCH_SIZE

    private val _logReadout = MutableStateFlow("hello message")
    val logReadout = _logReadout.asStateFlow()

    private var forceCancel = false

    init {
        val sysApiSize = System.getenv("API_BATCH_SIZE").toString().toInt()

        println("PaperlyzerApp.kt :: PaperlyzerApp :: init()")
        println("PaperlyzerApp.kt :: API() :: sysApiSize = $sysApiSize")
        println("PaperlyzerApp.kt :: API() :: API_BATCH_SIZE = $API_BATCH_SIZE")
        println("PaperlyzerApp.kt :: API() :: PROCESSED_RECORDS_GOAL = $UNPROCESSED_RECORDS_GOAL")

        forceCancel = false
        numConcurrentApiCalls = API_BATCH_SIZE

        initProcess(ProcessType.discipline)

        log.info("PaperlyzerApp. Process Name ::  :::::::::::::::::::")
        log.info("PaperlyzerApp. Process Name ::  ${process.name()}")
        log.info("PaperlyzerApp. Process Name ::  :::::::::::::::::::")

        File("test.json").writeText("hello json")

        log.info("\n\n build version: $BUILD_VERSION \n\n ")
    }

    fun initProcess(type: ProcessType) {
        matcher = TopicMatcher(topics)

        process = when (type) {
            ProcessType.citation -> WosCitationProcess(mongo, ::logMessage)
            ProcessType.discipline -> DisciplineProcess(mongo, matcher)
            ProcessType.wostoss -> WosToSsProcess(mongo, ::logMessage)
            ProcessType.report -> ReportProcess(mongo)
        }

        process.init()
    }

    fun start() {
        log.info("PaperlyzerApp.start()  ")
        forceCancel = false
        runProcess()
    }

    fun stop(outgoing: SendChannel<Frame>) {
        forceCancel = true
        println("PaperlyzerApp.kt :: stop() :: forceCancel = " + forceCancel)
        process.cancelJobs()
        stats(outgoing)
    }

    fun runProcess() {
        println("PaperlyzerApp.runWosProcess()  -- runProcess -- ")
        val time = measureTimeMillis {
            process.runProcess()
        }

        println("PaperlyzerApp.kt :: PaperlyzerApp :: runProcess :: time = $time")
        if (!forceCancel && process.shouldContinueProcess()) {
            runProcess()
        } else {
            println("PaperlyzerApp.kt :: runProcess() :: SHOULD NOT CONTINUE !!!  ")
            process.printStats()
        }
    }


    @DelicateCoroutinesApi
    fun handleSocketCommand(action: SocketAction?, outgoing: SendChannel<Frame>): String {
        println("PaperlyzerApp.kt :: PaperlyzerApp :: action = $action")
        WsBroadcaster.initWithOutgoing(outgoing)
        GlobalScope.launch {
            outgoing.send(Frame.Text("PaperlyzerApp.kt :: PaperlyzerApp :: action = $action"))
        }
        return when (action) {
            SocketAction.START -> {
                forceCancel = false
                log.info("PaperlyzerApp.handleCommand()  START ${process.name()}")
                GlobalScope.launch {
                    outgoing.send(Frame.Text("Starting batch process... :: ${process.name()}"))
                    logMessage("PaperlyzerApp.handleCommand()  START ${process.name()}")
                }
                GlobalScope.launch {
                    start()
                }
                "Starting WoS -> Semantic Scholar Paper Batch Process"
            }
            SocketAction.STOP -> {
                stop(outgoing)
                "Canceling Jobs"
            }
            SocketAction.STATS -> {
                process.printStats(outgoing)
                "Fetched Stats"
            }
            SocketAction.RESET -> {
                log.info("PaperlyzerApp.handleCommand()  RESET")
                process.reset()
                forceCancel = false
                process.printStats(outgoing)
                "Reset Process / Collections"
            }
            SocketAction.NA -> {
                print("socket command not recognized")
                "socket command not recognized"
            }
            else -> "socket command not recognized"
        }

    }

    fun report(): File {
        val stats = StatsController(mongo)
        return stats.runGenderedPaperReport()
    }

    fun stats(outgoing: SendChannel<Frame>) {
        println(":::::::::::::::::::::::::::::::::::: ")
        println(":::::::::::::::::::::::::::::::::::: ")
        println(":::::::::::::::::::::::::::::::::::: ")
        println(":::::::: build version: $BUILD_VERSION")
        println("processedWosPaperGoal: $UNPROCESSED_RECORDS_GOAL ")
        GlobalScope.launch {
            outgoing.send(Frame.Text("processedWosPaperGoal: $UNPROCESSED_RECORDS_GOAL "))
            outgoing.send(Frame.Text("batchSize: $numConcurrentApiCalls "))
        }
        println("batchSize: $numConcurrentApiCalls ")
        println(":::::::::::::::::::::::::::::::::::: ")
        process.printStats(outgoing)
        println(":::::::::::::::::::::::::::::::::::: ")
        println(":::::::::::::::::::::::::::::::::::: ")
        println(":::::::::::::::::::::::::::::::::::: ")
    }

    private fun logMessage(message: String) {
        _logReadout.value = "$message \n ${_logReadout.value}"
        print(logReadout.value)
    }

    fun updateServerUrl(url: String) {
        serverUrl = url
    }

}

object WsBroadcaster {

    private var outgoing: SendChannel<Frame>? = null

    fun initWithOutgoing(out: SendChannel<Frame>) {
        outgoing = out
    }

    fun broadcast(message: String) {
        GlobalScope.launch {
            outgoing?.send(Frame.Text(message))
        }
    }
}
