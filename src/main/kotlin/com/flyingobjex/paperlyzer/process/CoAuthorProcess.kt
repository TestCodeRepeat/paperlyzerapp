package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import org.litote.kmongo.ne

data class CoAuthorProcessStats(
    val totalProcessedWithCoAuthorData: Long,
    val totalUnprocessed: Long,
    val totalAuthors: Long,
    val totalUnidentified: Long,
) {
    override fun toString(): String {
        return ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "!!     CoAuthor / Author Process      !!" +
            "!!     CoAuthor / Author      !!" +
            "\n\ntotalProcessedWithCoAuthorData: $totalProcessedWithCoAuthorData \n" +
            "totalUnprocessed: $totalUnprocessed \n" +
            "totalAuthors: $totalAuthors \n" +
            "totalUnidentified: $totalUnidentified \n" +
            "UNPROCESSED_RECORDS_GOAL: $UNPROCESSED_RECORDS_GOAL \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n"
    }
}

@DelicateCoroutinesApi
class CoAuthorProcess(val mongo: Mongo) : IProcess {

    val log: Logger = Logger.getAnonymousLogger()
    private val authorRepo = AuthorRepository(mongo)
    private val wosRepo = WoSPaperRepository(mongo)

    override fun init() {
        log.info("CoAuthorProcess.init()")
    }

    override fun name(): String = "CoAuthor / Author Process"

    override fun runProcess() {
        val batchSize = API_BATCH_SIZE
        log.info("WosCitationProcess.runProcess()  0000 :: batchSize = $batchSize")
        var unprocessed = emptyList<Author>()
        val time = measureTimeMillis {
            unprocessed = authorRepo.getUnprocessedAuthorsByCoAuthors(batchSize)
        }

        unprocessed.parallelStream().forEach { author ->

            val associatedPapers = wosRepo.getPapers(author.papers?.map { it.doi } ?: emptyList())
            val totalPapers = associatedPapers.size
            val totalAllAuthors = associatedPapers.sumOf { it.authors.size }
            val totalCoAuthors = totalAllAuthors - associatedPapers.size
            val averageCoAuthors = totalCoAuthors.toDouble() / totalPapers.toDouble()

            authorRepo.updateAuthor(author.copy(totalPapers = totalPapers, averageCoAuthors = averageCoAuthors))
        }

        log.info("CoAuthorProcess.runProcess()  time = $time")

    }

    override fun shouldContinueProcess(): Boolean {
        var shouldContinue = false
        val unprocessedCount = authorRepo.unprocessedAuthorsCount()
        log.info("CoAuthorProcess.shouldContinueProcess()  unprocessedCount = $unprocessedCount")
        val time = measureTimeMillis {
            shouldContinue = unprocessedCount > UNPROCESSED_RECORDS_GOAL
        }
        log.info("CoAuthorProcess.shouldContinueProcess()  time = $time ms")
        return shouldContinue
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        log.info("CoAuthorProcess.printStats()  Stats starting.... ")
        val stats = authorRepo.getCoAuthorStats()
        log.info("WosCitationProcess.printStats()  stats = $stats")
        GlobalScope.launch {
            outgoing?.send(Frame.Text(stats.toString()))
        }
        return stats.toString()
    }

    override fun cancelJobs() {
        TODO("Not yet implemented")
    }

    override fun reset() {
        log.info("CoAuthorProcess.reset()  ")
        val time = measureTimeMillis {
            authorRepo.resetCoAuthorData()
        }
        log.info("CoAuthorProcess.reset()  completed in $time ms")
    }


}
