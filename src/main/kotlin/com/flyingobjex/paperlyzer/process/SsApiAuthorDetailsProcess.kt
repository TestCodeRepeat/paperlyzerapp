package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.api.SEMANTIC_SCHOLAR_API_KEY
import com.flyingobjex.paperlyzer.api.SemanticScholarAPI
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.repo.SemanticScholarAuthorRepo
import io.ktor.http.cio.websocket.*
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SsApiAuthorDetailsProcess(val mongo: Mongo) : IProcess {

    val authorRepo = SemanticScholarAuthorRepo(mongo)
    val api = SemanticScholarAPI(SEMANTIC_SCHOLAR_API_KEY)
    var duplicatAuthorIdCount = 0
    var currentUnprocessedCount = 0

    override fun init() {
        println("SsApiAuthorDetailsProcess.kt :: init :: ")
    }

    override fun name(): String = "SsApiAuthorDetailsProcess"

    override fun runProcess() {
        // fetch all authors for ss papers
        val batchSize = API_BATCH_SIZE
        var unprocessed: List<WosPaper>
        val time = measureTimeMillis {
            unprocessed = authorRepo.getUnprocessedRawPapersBySsAuthorDetails(batchSize)
        }

        println("SsApiAuthorDetailsProcess.kt :: load unprocessed :: time = $time")

        unprocessed.parallelStream().forEach { wosPaper ->
            wosPaper.ssAuthors?.parallelStream()?.forEach { ssAuthorData ->
                ssAuthorData.authorId?.let { authorId ->
                    if (authorRepo.ssAuthorAlreadyExists(authorId)) {
                        duplicatAuthorIdCount++
                        println("SsApiAuthorDetailsProcess.kt :: Ss Author Already Exists! :: $duplicatAuthorIdCount :: UNPROCESSED COUNT = $currentUnprocessedCount")
                    } else {
                        runBlocking {
                            launch(IO) {
                                api.authorById(authorId)?.let { ssAuthorDetails ->
                                    authorRepo.addSsAuthorDetails(ssAuthorDetails)
                                } ?: kotlin.run {
                                    println("SsApiAuthorDetailsProcess.kt :: API ERROR() :: authorId = $authorId")

                                }
                            }
                        }
                    }
                }
            }

            authorRepo.updateRawPaperWithSsAuthorStep2(wosPaper._id)
        }
    }

    override fun shouldContinueProcess(): Boolean {
        println("SsApiAuthorDetailsProcess.kt :: shouldContinueProcess 0000 :: ?????? .........")
        val res = authorRepo.getUnprocessedRawPapersBySsAuthorDetailsCount()
        currentUnprocessedCount = res
        println("SsApiAuthorDetailsProcess.kt :: shouldContinueProcess() 1111 :: res = $res")
        val shouldContinue = res > UNPROCESSED_RECORDS_GOAL
        if (!shouldContinue) {
            println("SsApiAuthorDetailsProcess.shouldContinueProcess()  SHOULD NOT CONTINUE, BAIL OUT")
            printStats()
        }
        return shouldContinue
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        val stats = authorRepo.getSsApiAuthorDetailsStats().toString()
        println(
            "SsApiAuthorDetailsProcess.kt :: printStats() :: stats \n" +
                "!!!! DUPLICATE ID COUNT = $duplicatAuthorIdCount \n" +
                " $stats"
        )
        runBlocking {
            outgoing?.send(
                Frame.Text(
                    stats +
                        "\"!!!! DUPLICATE ID COUNT = $duplicatAuthorIdCount \\n\" +" +
                        "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                )
            )
        }
        return stats
    }


    override fun cancelJobs() {
        TODO("Not yet implemented")
    }

    override fun reset() {
        println("SsApiAuthorDetailsProcess.kt :: reset :: ")
        authorRepo.resetSsAuthorDataStep2()
    }

    override fun type(): ProcessType = ProcessType.SsApiAuthor
}
