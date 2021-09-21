package com.flyingobjex.paperlyzer.control

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.api.SEMANTIC_SCHOLAR_API_KEY
import com.flyingobjex.paperlyzer.api.SemanticScholarAPI
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.repo.SemanticScholarPaperRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.repo.WosPaperId
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.or


data class WosToSSProcessBatchStats(
    val totalSsPapers: Int,
    val totalProcessedWosPapers: Int,
    val totalFailedWosPapers: Int,
    val totalWosPapers: Int,
) {
    override fun toString(): String {
        return "\n totalSsPapers: $totalSsPapers \n" +
            "totalProcessWosPapers: $totalProcessedWosPapers \n" +
            "totalFailedWosPapers: $totalFailedWosPapers \n" +
            "totalWosPapers: $totalWosPapers \n"
    }

    fun print() {
        println("totalSsPapers: $totalSsPapers")
        println("totalProcessWosPapers: $totalProcessedWosPapers")
        println("totalFailedWosPapers: $totalFailedWosPapers")
        println("totalWosPapers: $totalWosPapers")
    }
}

class SSPaperController(val mongo: Mongo, logMessage: (message: String) -> Unit) {
    val log: Logger = Logger.getAnonymousLogger()

    private val wosPaperRepo = WoSPaperRepository(mongo, logMessage)
    private val ssPaperRepo = SemanticScholarPaperRepository(mongo)
    private val api = SemanticScholarAPI(SEMANTIC_SCHOLAR_API_KEY)

    var mainJobs: MutableList<Job>? = null
    var jobs: MutableList<Job>? = null

    val batchSize = 31

    fun cancelJobs() {
        jobs?.forEach {
            it.cancel()
        }
        mainJobs?.forEach {
            it.cancel()
        }
    }

    fun reset(){
        wosPaperRepo.resetSsProcessed()
    }

    fun processWosBatchGrouped(apiBatchSize: Int, numberOfGroups: Int) {
        log.info("SSPaperController.processWosBatch()  1111")
        val fullPaperSet = wosPaperRepo.getUnprocessedPapersAsPaperIds(apiBatchSize * numberOfGroups)
        val groups = mutableListOf<List<WosPaperId>>()
        var n = 0
        for (i in 1..numberOfGroups) {
            val start = n
            val end = n + apiBatchSize
            groups.add(fullPaperSet.subList(start, end))
            n += apiBatchSize
        }

        if (fullPaperSet.size == 0) {
            throw Error("SSPaperController.kt :: processWosBatch :: NO PAPERS LOADED !!!!!")
        }

        log.info("SSPaperController.processWosBatch()  2222")
        runBlocking {
            launch(IO) {
                groups.forEach { papers ->
                    papers.parallelStream().forEach { wosPaperId ->
                        runBlocking {
                            launch(IO) {
                                api.fetchSsPaperByDoi(wosPaperId.doi)?.let { ssPaper ->
                                    val withOriginalDoi = ssPaper.copy(wosDoi = wosPaperId.doi)
                                    ssPaperRepo.insertPaper(withOriginalDoi)
                                    wosPaperRepo.markSsAsProcessedById(wosPaperId)
                                } ?: run {
                                    wosPaperRepo.markSsAsFailedById(wosPaperId)
                                }
                            }
                        }
                    }
                    delay(1000)
                }
            }
        }
        log.info("SSPaperController.processWosBatch()  4444")
    }

    fun getProcessedWosPaperCount(): Int =
        mongo.genderedPapers.countDocuments(
            WosPaper::ssProcessed eq true
        ).toInt()

    fun getUnprocessedWosPaperCount(): Int =
        mongo.genderedPapers.countDocuments(
            and(
                or(
                    WosPaper::ssProcessed eq null,
                    WosPaper::ssProcessed ne true,
                ),
                WosPaper::ssFailed ne true,
                WosPaper::doi ne "NA",
            )
        ).toInt()


    fun getStats(): WosToSSProcessBatchStats {
        return WosToSSProcessBatchStats(
            mongo.ssPapers.countDocuments().toInt(),
            mongo.genderedPapers.countDocuments(WosPaper::ssProcessed eq true).toInt(),
            mongo.genderedPapers.countDocuments(WosPaper::ssFailed eq true).toInt(),
            mongo.genderedPapers.countDocuments().toInt()
        )
    }


    /** =========== GRAVEYARD ============ */
    /** =========== GRAVEYARD ============ */
    /** =========== GRAVEYARD ============ */

    fun processWosBatch(batchSize: Int) {
        log.info("SSPaperController.processWosBatch()  1111")
        val papers = wosPaperRepo.getUnprocessedPapersAsPaperIds(batchSize)
        if (papers.size == 0) {
            throw Error("SSPaperController.kt :: processWosBatch :: NO PAPERS LOADED !!!!!")
        }
        log.info("SSPaperController.processWosBatch()  2222")
        runBlocking {
            launch(IO) {
                log.info("SSPaperController.processWosBatch()  3333")
                papers.forEachIndexed { index, wosPaperId ->
                    log.info("SSPaperController.processWosBatch() wosPaperId  = ${wosPaperId}")
                    api.fetchSsPaperByDoi(wosPaperId.doi)?.let { ssPaper ->
                        ssPaperRepo.insertPaper(ssPaper)
                        wosPaperRepo.markSsAsProcessedById(wosPaperId)
                    } ?: run {
                        wosPaperRepo.markSsAsFailedById(wosPaperId)
                    }
                }
            }
        }
        log.info("SSPaperController.processWosBatch()  4444")
    }

    fun getStats(outgoing: SendChannel<Frame>) {
        val totalSsPapers = mongo.ssPapers.countDocuments()
        val totalProcessWosPapers = mongo.genderedPapers.countDocuments(WosPaper::ssProcessed eq true)
        val totalFailedWosPapers = mongo.genderedPapers.countDocuments(WosPaper::ssFailed eq true)
        val totalWosPapers = mongo.genderedPapers.countDocuments()
        runBlocking {
            launch(IO) {
                outgoing.send(Frame.Text("Semantic Scholar Papers: $totalSsPapers"))
                outgoing.send(Frame.Text("Processed Web of Science Papers: $totalProcessWosPapers"))
                outgoing.send(Frame.Text("Failed to process W of S Papers: $totalFailedWosPapers"))
                outgoing.send(Frame.Text("totalWosPapers: $totalWosPapers"))
            }
        }

    }

    fun getSsPapersCount(): Int = mongo.ssPapers.countDocuments().toInt()

//    fun resetWosPaperTable() {
//        mongo.genderedPapers.updateMany(
//            WosPaper::ssProcessed eq true,
//            setValue(WosPaper::ssProcessed, false)
//        )
//    }

}
