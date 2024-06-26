package com.flyingobjex.paperlyzer

import com.flyingobjex.paperlyzer.control.Stats
import com.flyingobjex.paperlyzer.domain.topics
import com.flyingobjex.paperlyzer.parser.SJRModel
import com.flyingobjex.paperlyzer.parser.TopicMatcher
import com.flyingobjex.paperlyzer.process.*
import com.flyingobjex.paperlyzer.process.reports.AuthorReportProcess
import com.flyingobjex.paperlyzer.process.reports.PaperReportProcess
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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi

var API_BATCH_SIZE = System.getenv("API_BATCH_SIZE").toString().toInt()
var UNPROCESSED_RECORDS_GOAL = System.getenv("UNPROCESSED_RECORDS_GOAL").toString().toInt()
var BASE_URL = "localhost:8080"

enum class ProcessType {
    Citation, Discipline, WoSToSs, PaperReport, SJR, CoAuthor, AuthorReport, StemSsh, Initialization, SsAuthor, SsApiAuthor
}

const val BUILD_VERSION = 3

@OptIn(ExperimentalSerializationApi::class)
class PaperlyzerApp(val mongo: Mongo, val theProcess:IProcess? = null) {

    var process = theProcess ?: throw Error("!!!! No Process Attached !!!!!")
    val log: Logger = Logger.getAnonymousLogger()

    private var port: String = "na"
    private var matcher = TopicMatcher(topics)

    /** batchSize: # of concurrent API calls */
    var numConcurrentApiCalls: Int = API_BATCH_SIZE

    private val _logReadout = MutableStateFlow("hello message")
    val logReadout = _logReadout.asStateFlow()

    private var forceCancel = false

    init {
        try {
            BASE_URL = System.getenv("BASE_URL").toString()
        } catch (e: Exception) {
            log.info("PaperlyzerApp () BASE_URL not set, default to localhost  ")
        }

        println("PaperlyzerApp.kt :: PaperlyzerApp :: init()")
        println("PaperlyzerApp.kt :: API() :: API_BATCH_SIZE = $API_BATCH_SIZE")
        println("PaperlyzerApp.kt :: API() :: PROCESSED_RECORDS_GOAL = $UNPROCESSED_RECORDS_GOAL")

        forceCancel = false
        numConcurrentApiCalls = API_BATCH_SIZE

        log.info("PaperlyzerApp. Process Name ::  :::::::::::::::::::")
        log.info("PaperlyzerApp. Process Name ::  ${process.name()}")
        log.info("PaperlyzerApp. Process Name ::  :::::::::::::::::::")

        File("test.json").writeText("hello json")

        log.info("\n\n build version: $BUILD_VERSION \n\n ")
        log.info("PaperlyzerApp.()  url = ${url()}")
    }

    fun manuallySetUnprocessedRecordsGoal(goal: Int) {
        UNPROCESSED_RECORDS_GOAL = goal
        log.info(
            "PaperlyzerApp.manuallySetUnprocessedRecordsGoal()" +
                "\n !! MANUALLY UPDATED UNPROCESSED_RECORDS_GOAL !!" +
                "\n UNPROCESSED_RECORDS_GOAL = $UNPROCESSED_RECORDS_GOAL" +
                "\n"
        )
    }

    fun maunallySetApiBatchSize(batchSize: Int) {
        API_BATCH_SIZE = batchSize
    }

    fun initData() {
        runBlocking {
//            val res = hIndexModel.loadHindexData()
//            if (res != null){
//                print(res)
//            }
        }
    }

    fun url(): String = "$BASE_URL/"

    fun overrideProcessWith(type: ProcessType) {
        matcher = TopicMatcher(topics)
        process = when (type) {
            ProcessType.Citation -> WosCitationProcess(mongo)
            ProcessType.Discipline -> DisciplineProcess(mongo, matcher)
            ProcessType.WoSToSs -> WosToSsProcess(mongo, ::logMessage)
            ProcessType.PaperReport -> PaperReportProcess(mongo)
            ProcessType.SJR -> SJRProcess(mongo)
            ProcessType.CoAuthor -> CoAuthorProcess(mongo)
            ProcessType.AuthorReport -> AuthorReportProcess(mongo)
            ProcessType.StemSsh -> AuthorStemSshProcess(mongo)
            ProcessType.Initialization -> InitializationProcess(mongo)
            ProcessType.SsAuthor -> SsAuthorToRawPaperProcess(mongo)
            ProcessType.SsApiAuthor -> SsApiAuthorDetailsProcess(mongo)
        }
        process.init()
    }

    fun start() {
        if (process == null){
            log.info("PaperlyzerApp.start()  !!!!!!!!! NO PROCESS SET !!!!!!!!!!!!")
            log.info("PaperlyzerApp.start()  !!!!!!!!! NO PROCESS SET !!!!!!!!!!!!")
            log.info("PaperlyzerApp.start()  !!!!!!!!! NO PROCESS SET !!!!!!!!!!!!")
            log.info("PaperlyzerApp.start()  !!!!!!!!! NO PROCESS SET !!!!!!!!!!!!")
            log.info("PaperlyzerApp.start()  !!!!!!!!! NO PROCESS SET !!!!!!!!!!!!")
        }
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

        println("PaperlyzerApp.kt :: PaperlyzerApp :: runProcess :: time = $time \n")
        if (!forceCancel && process.shouldContinueProcess() == true) {
            println("PaperlyzerApp.kt :: runProcess() :: +++ SHOULD CONTINUE +++  ")
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
                "Starting Process :: ${process.name()}"
            }
            SocketAction.STOP -> {
                stop(outgoing)
                "Canceling Jobs"
            }
            SocketAction.STATS -> {
                GlobalScope.launch {
                    outgoing.send(
                        Frame.Text(
                            "::::::::::::::::::::::::::::::::::::::::::::::::\n" +
                                "STATS -> Printing Stats for ${process.name()} \n" +
                                ":::::::::::::::::::::::::::::::::::::::::::::::: \n"
                        )
                    )
                }
                log.info(
                    "::::::::::::::::::::::::::::::::::::::::::::::::\n" +
                        "STATS -> Printing Stats for ${process.name()} \n" +
                        "::::::::::::::::::::::::::::::::::::::::::::::::\n"
                )

                process.printStats(outgoing)
                "Fetched Stats"
            }
            SocketAction.RESET -> {
                log.info("PaperlyzerApp.handleCommand()  RESET ${process.name()}")
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
        val stats = Stats(mongo)
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

    fun updateServerPort(port: String) {
        this.port = port
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
