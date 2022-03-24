package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.entity.Author
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.channels.SendChannel

class ReviewPolicyProcess(val mongo: Mongo):IProcess {
    val log: Logger = Logger.getAnonymousLogger()

    override fun init() {
        println("ReviewPolicyProcess.kt :: init :: ")
    }

    override fun name(): String = "Review Policy Process"

    override fun runProcess() {
        val batchSize = API_BATCH_SIZE
        log.info("CoAuthorProcess.runProcess()  :: batchSize = $batchSize")
        var unprocessed: List<Author>
        val time = measureTimeMillis {
            unprocessed = coAuthorUseCase.getUnprocessedAuthorsByCoAuthors(batchSize)
        }


    }

    override fun shouldContinueProcess(): Boolean {
        TODO("Not yet implemented")
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        TODO("Not yet implemented")
    }

    override fun cancelJobs() {
        TODO("Not yet implemented")
    }

    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun type(): ProcessType {
        TODO("Not yet implemented")
    }


}
