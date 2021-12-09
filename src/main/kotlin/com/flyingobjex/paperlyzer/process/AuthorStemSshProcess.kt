package com.flyingobjex.paperlyzer.process

import com.flyingobjex.paperlyzer.API_BATCH_SIZE
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.UNPROCESSED_RECORDS_GOAL
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.IWosPaperWithStemSsh
import com.flyingobjex.paperlyzer.parser.DisciplineType
import com.flyingobjex.paperlyzer.process.DisciplineUtils.calculateStemSshScores
import com.flyingobjex.paperlyzer.process.DisciplineUtils.disciplineScoreToDiscipline
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import io.ktor.http.cio.websocket.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch


data class StemSshAuthorProcessStats(
    val totalProcessedWithStemSshData: Long,
    val totalUnprocessed: Long,
    val totalStem: Long,
    val totalSsh: Long,
    val totalM: Long,
    val totalUnidentified: Long,
    val totalAuthors: Long,
) {
    override fun toString(): String {
        return ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::  \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "!!     CoAuthor / Author Process      !!" +
            "!!     CoAuthor / Author      !!" +
            "\n\ntotalProcessedWithStemSshData: $totalProcessedWithStemSshData \n" +
            "totalUnprocessed: $totalUnprocessed \n" +
            "totalStem: $totalStem \n" +
            "totalSsh: $totalSsh \n" +
            "totalAuthors: $totalAuthors \n" +
            "totalM: $totalM \n" +
            "totalUnidentified: $totalUnidentified \n" +
            "UNPROCESSED_RECORDS_GOAL: $UNPROCESSED_RECORDS_GOAL \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n" +
            "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: \n"
    }
}


class AuthorStemSshProcess(val mongo: Mongo) : IProcess {

    val log: Logger = Logger.getAnonymousLogger()
    private val authorRepo = AuthorRepository(mongo)
    private val wosRepo = WoSPaperRepository(mongo)

    override fun init() {
        log.info("AuthorStemSshProcess.init()")
    }

    override fun name(): String = "Author STEM / SSH Process"

    override fun runProcess() {
        log.info("AuthorStemSshProcess.runProcess()  :: batchSize = $API_BATCH_SIZE")
        var unprocessed = emptyList<Author>()
        val time = measureTimeMillis {
            unprocessed = authorRepo.getUnprocessedAuthorsByStemSsh(API_BATCH_SIZE)
        }

        log.info("\n\nAuthorStemSshProcess.runProcess() fetch unprocessed ::  time = $time \n\n")
        val allShortTitles = unprocessed
            .map { unProcessedAuthor ->
                unProcessedAuthor.papers?.map { it.shortTitle } ?: emptyList()
            }
            .flatten()

        log.info("AuthorStemSshProcess.runProcess()  allShortTitles.size = ${allShortTitles.size}")
        val allAssociatedPapers = wosRepo.getPapersWithStemSsh(allShortTitles)

        unprocessed.parallelStream().forEach { author ->
            val associatedPapers = author.papers?.mapNotNull {
                getAssociatedPapersForStemSsh(allAssociatedPapers, it.shortTitle)
            } ?: emptyList()

            val stemSshScore = calculateStemSshScores(associatedPapers)
            authorRepo.updateAuthor(
                author.copy(
                    disciplineScore = stemSshScore,
                    discipline = disciplineScoreToDiscipline(stemSshScore)
                )
            )
        }
    }

    override fun shouldContinueProcess(): Boolean {
        var shouldContinue: Boolean
        val time = measureTimeMillis {
            val unprocessedCount = authorRepo.getUnprocessedAuthorsByAStemSshCount()
            log.info("AuthorStemSshProcess.shouldContinueProcess()  unprocessedCount = $unprocessedCount")
            shouldContinue = unprocessedCount > UNPROCESSED_RECORDS_GOAL
        }
        log.info("CoAuthorProcess.shouldContinueProcess()  time = $time ms")
        return shouldContinue
    }

    override fun printStats(outgoing: SendChannel<Frame>?): String {
        log.info("CoAuthorProcess.printStats()  Stats starting.... ")
        val stats = authorRepo.getStemSshAuthorStats()
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
        authorRepo.resetStemSsh()
        printStats()
    }

    override fun type(): ProcessType = ProcessType.StemSsh

}

object DisciplineUtils {
    fun disciplineScoreToDiscipline(score: Double?): DisciplineType {
        if (score == null) return DisciplineType.NA
        return if (score <= .45) {
            DisciplineType.STEM
        } else if (score > .45 && score <= .55) {
            DisciplineType.M
        } else {
            DisciplineType.SSH
        }
    }

    fun calculateStemSshScores(associatedPapers: List<IWosPaperWithStemSsh>): Double? {
        val res = associatedPapers
            .mapNotNull { disciplineToScore(it.discipline) }

        return if (res.isEmpty()) null
        else res.sumOf { it } / associatedPapers.size
    }

    fun disciplineToScore(disciplineType: DisciplineType?): Double? =
        when (disciplineType) {
            DisciplineType.STEM -> 0.0
            DisciplineType.SSH -> 1.0
            DisciplineType.M -> 0.5
            DisciplineType.NA -> null
            DisciplineType.UNINITIALIZED -> null
            null -> null
        }
}
