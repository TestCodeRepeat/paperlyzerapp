package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.WosPaperWithAuthors
import com.flyingobjex.paperlyzer.entity.WosPaperWithStemSsh
import com.flyingobjex.paperlyzer.repo.*
import com.flyingobjex.paperlyzer.util.GenderUtils.allPapersAreGenderComplete
import com.flyingobjex.paperlyzer.util.GenderUtils.averageGenderRatio
import com.flyingobjex.paperlyzer.util.GenderUtils.averageGenderRatioOfAuthors
import com.flyingobjex.paperlyzer.util.GenderUtils.toGenderRatio
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

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

fun getAssociatedPapersForStemSsh(papers: List<WosPaperWithStemSsh>, shortTitle: String) =
    papers.firstOrNull { it.shortTitle == shortTitle }

fun getAssociatedPaper(papers: List<WosPaperWithAuthors>, doi: String) =
    papers.firstOrNull { it.doi == doi }


/**
 *  Determines an Author's average # of co-authors
 *  Determines # of times Author is First Author from WoS Papers
 *
 * uses
 *  Wos Paper Collection
 *  Author Collection
 * */
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
        log.info("CoAuthorProcess.runProcess()  :: batchSize = $batchSize")
        var unprocessed = emptyList<Author>()
        val time = measureTimeMillis {
            unprocessed = authorRepo.getUnprocessedAuthorsByCoAuthors(batchSize)
        }

        log.info("\n\nCoAuthorProcess.runProcess() fetch unprocessed ::  time = $time \n\n")

        val allShortTitles =
            unprocessed.map { unProcessedAuthor -> unProcessedAuthor.papers?.map { it.shortTitle } ?: emptyList() }
                .flatten()
        log.info("CoAuthorProcess.runProcess()  allDOis.size = ${allShortTitles.size}")

        val allAssociatedPapers: List<WosPaperWithAuthors> = wosRepo.getPapers(allShortTitles)
        log.info("CoAuthorProcess.runProcess()   allAssociatedPapers: ${allAssociatedPapers.size}")

        val averageGenderRatioOfPapers: Double?
        val genderRatioOfAllCoAuthors: Double?
        if (allPapersAreGenderComplete(allAssociatedPapers)) {
            averageGenderRatioOfPapers = averageGenderRatio(allAssociatedPapers)
            genderRatioOfAllCoAuthors = averageGenderRatioOfAuthors(
                allAssociatedPapers.map { it.authors }.flatten()
            )
        } else {
            averageGenderRatioOfPapers = -5.0
            genderRatioOfAllCoAuthors = -5.0
        }

        unprocessed.parallelStream().forEach { author ->
            val associatedPapers =
                author.papers?.mapNotNull { getAssociatedPaper(allAssociatedPapers, it.doi) } ?: emptyList()
            val totalPapers = associatedPapers.size
            val totalAllAuthors = associatedPapers.sumOf { it.totalAuthors ?: 0 }
            val totalPapersAsFirstAuthor =
                firstAuthorCount(associatedPapers, author.lastName, author.firstName.toString())
            val totalCoAuthors = totalAllAuthors - totalPapers
            val averageCoAuthors = totalCoAuthors.toDouble() / totalPapers.toDouble()
            authorRepo.updateAuthorCoAuthors(
                author.copy(
                    totalPapers = totalPapers,
                    averageCoAuthors = averageCoAuthors,
                    firstAuthorCount = totalPapersAsFirstAuthor.toLong(),
                    averageGenderRatioOfPapers = averageGenderRatioOfPapers,
                    genderRatioOfAllCoAuthors = genderRatioOfAllCoAuthors,
                )
            )
        }
    }

    private fun firstAuthorCount(
        associatedPapers: List<WosPaperWithAuthors?>,
        lastName: String,
        firstName: String
    ): Int {
        val res = associatedPapers.filter { paper ->
            paper?.authors?.firstOrNull()?.let { first ->
                return@filter first.firstName?.trim()?.lowercase().equals(firstName.trim().lowercase())
                    && first.lastName.trim().lowercase() == lastName.trim().lowercase()
            } ?: run {
                false
            }
        }
        return res.size
    }

    override fun shouldContinueProcess(): Boolean {
        var shouldContinue = false
        val time = measureTimeMillis {
            val unprocessedCount = authorRepo.unprocessedCoAuthorsCount()
            log.info("CoAuthorProcess.shouldContinueProcess()  unprocessedCount = $unprocessedCount")
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

    override fun type(): ProcessType = ProcessType.coauthor
}
