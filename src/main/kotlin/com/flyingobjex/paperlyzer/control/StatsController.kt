package com.flyingobjex.paperlyzer.control

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.*
import com.flyingobjex.paperlyzer.parser.CSVHelper
import com.flyingobjex.paperlyzer.parser.DisciplineType
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.JournalTableRepo
import java.io.File
import java.text.NumberFormat
import java.util.*
import java.util.logging.Logger
import kotlin.streams.asSequence
import kotlinx.serialization.Serializable
import org.litote.kmongo.*

data class YearsPublishedResult(val _id: String, val papers: List<String>)

data class AuthorYearsPublised(val yearsPublishedCountByYear: HashMap<Int, Int>? = null)

data class AuthorTableStats(
    val totalAuthors: Long,
    val totalWithNoAssignedGender: Int,
    val totalFemaleAuthors: Int,
    val totalMaleAuthors: Int,
)

data class GenderTableStats(
    val totalAuthors: Long? = null,
    val totalWithNoAssignedGender: Int? = null,
    val totalFemaleNames: Int? = null,
    val totalMaleNames: Int? = null,
)

data class JournalCount(val name: String, val numOfCitations: Int)

data class JournalTableStats(
    val totalJournals: Int,
    val top50Journals: List<JournalCount>,
)

@Serializable
data class AuthorReportLine(
    val firstName: String,
    val lastName: String,
    val gender: GenderIdentitiy,
    val genderProbability: Double,
    val totalPapers: Int,
    val totalFirstAuthored:Int,
    val yearsPublished: String,
    val firstYearPublished: Int,
    val lastYearPublished: Int,
    val publishedTitles: String,
    val orcID: String?,
    val coAuthorAverage:Double,
    val discipline: DisciplineType,
    val disciplineScore:Double,
)

@Serializable
data class PaperReportLine(
    val shortTitle: String,
    val authors: String,
    val year: String,
    val title: String,
    val journal: String,
    val text_type: String,
    val keywords: String,
    val emails: String,
    val orcIds: String,
    val doi: String,
    val diszOrigTopic: String,
    var gendersShortKey: String? = null,
    var firstAuthorGender: String? = null,
    var lastAuthorGender: String? = null,
    var genderCompletenessScore: Double? = null,
    var totalAuthors: Int? = null,
    var totalIdentifiableAuthors: Int? = null,
    var citationsCount: Int? = null,
    var influentialCitationsCount: Int? = null,
    var discipline: DisciplineType? = null,
    var discScore: Int? = null,
    val discTopic: String?,
    val sjrRank:Int? = null,
    val hIndex:Int? = null,
//    var discTopSSH: String? = null,
//    val discTopSTEM: String? = null,
)

fun Int.read(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)!!
}

class GlobalStats(
    val totalAuthors: Int = 0,
    val totalWithNoAssignedGender: Int = 0,
    val totalFemaleNames: Int = 0,
    val totalMaleNames: Int = 0,
    val totalFirstNames: Int = 0,
    val totalJournals: Int = 0,
) {

    override fun toString(): String {
        return """
            Total Authors: ${totalAuthors.read()}
            Authors w/ Gender: ${(totalFemaleNames + totalFemaleNames).read()}
            Authors (ambiguous): ${(totalAuthors - totalFemaleNames - totalMaleNames).read()}
            Total Female Authors: ${totalFemaleNames.read()}
            Total Male Authors: ${totalMaleNames.read()}
            Total Journals: ${totalJournals.read()}
        """.trimIndent()
    }

    companion object {
        fun toStats(
            genderedAuthorsTable: AuthorTableStats,
            totalFirstNames: Int,
            journalTableStats: JournalTableStats
        ): GlobalStats {
            return GlobalStats(
                genderedAuthorsTable.totalAuthors.toInt(),
                genderedAuthorsTable.totalWithNoAssignedGender,
                genderedAuthorsTable.totalFemaleAuthors,
                genderedAuthorsTable.totalMaleAuthors,
                totalFirstNames,
                journalTableStats.totalJournals
            )
        }
    }
}

class StatsController(val mongo: Mongo) {

    val log: Logger = Logger.getAnonymousLogger()

    private val authorRepo = AuthorRepository(mongo)
    private val journalRepo = JournalTableRepo(mongo)

    fun runGlobalStatsReport(): GlobalStats = GlobalStats.toStats(
        statsGenderedAuthorsTable(),
        firstNamesTableTotalNames(),
        statsJournalTable()
    )


    fun getUnprocessedPapersByReport(batchSize: Int) =
        mongo.genderedPapers.find(WosPaper::reported ne true).limit(batchSize).toList()

    fun addReports(reports: List<PaperReportLine>) {
        mongo.reports.insertMany(reports)
        log.info("StatsCoordinator.addReports()  DONE")
    }

    fun markAsReported(unprocessed: List<WosPaper>) {
        mongo.genderedPapers.updateMany(
            WosPaper::_id `in` unprocessed.map { it._id },
            setValue(WosPaper::reported, true)
        )
    }

    fun papersToReportLines(papers: List<WosPaper>): List<PaperReportLine> = papers.map {
        PaperReportLine(
            shortTitle = it.shortTitle,
            authors = it.authors.map { "${it.firstName} ${it.lastName} " }.joinToString(", "),
            year = it.year,
            title = it.title,
            journal = it.journal,
            text_type = it.text_type,
            keywords = it.keywords,
            emails = it.emails,
            orcIds = it.orcid,
            doi = it.doi,
            gendersShortKey = it.authorGendersShortKey,
            firstAuthorGender = it.firstAuthorGender,
            lastAuthorGender = it.withoutFirstAuthorGender,
            genderCompletenessScore = it.genderCompletenessScore,
            totalAuthors = it.totalAuthors,
            totalIdentifiableAuthors = it.totalIdentifiableAuthors,
            citationsCount = it.citationsCount,
            influentialCitationsCount = it.influentialCitationsCount,
            discipline = it.discipline,
            discScore = it.score,
            discTopic = it.matchingCriteria?.sortedByDescending { it.score }?.firstOrNull()?.term,
            diszOrigTopic = it.topics.joinToString("; "),
            sjrRank = it.sjrRank,
            hIndex = it.hIndex,
        )
    }

    /** Disciplines */
    fun resetDisciplinesReport() {
        mongo.reports.drop()
    }

    fun runPapersWithDisciplinesReport() {
        log.info("StatsCoordinator.runPapersWithDisciplinesReport()  START!")
        val asReportLines = papersToReportLines(emptyList())
        log.info("StatsCoordinator.runPapersWithDisciplinesReport()  UPDATE!")
        asReportLines.parallelStream().forEach {
            mongo.reports.insertOne(it)
        }
        log.info("StatsCoordinator.runPapersWithDisciplinesReport()  DONE!")
    }


    fun runGenderedPaperReport(): File {
        val reportLines = papersToReportLines(emptyList())
        return CSVHelper.writeCsvFile(reportLines.sortedByDescending { it.journal }, "genderedPapers")
    }

    fun runGenderedAuthorReport(): File {
        val querySize = 500000
        val batch: List<Author> = authorRepo.getGenderedAuthors(querySize)

        val report = mutableListOf<AuthorReportLine>()
        batch.parallelStream().asSequence().filterNotNull().forEach { author ->
            val years = author.toYearsPublished()
                .mapNotNull { it.toIntOrNull() }
                .sorted()

            report.add(
                AuthorReportLine(
                    author.firstName ?: "",
                    author.lastName,
                    author.gender.gender,
                    author.gender.probability,
                    author.paperCount,
                    author.firstAuthorCount?.toInt() ?: 0,
                    years.joinToString(";"),
                    years.firstOrNull() ?: 0,
                    years.lastOrNull() ?: 0,
                    author.publishedTitles().joinToString(";"),
                    author.orcIDString,
                    author.averageCoAuthors ?: -5.5,
                    author.discipline ?: DisciplineType.NA,
                    author.disciplineScore ?: -5.5
                )
            )
        }

        return CSVHelper.writeCsvFile(report.sortedByDescending { it.totalPapers }, "genderReveal")
    }

    /** Journal Stats */
    fun statsJournalTable(): JournalTableStats {
        val allJournals = journalRepo.mongo.journals.find().toList().groupBy { it.journalName }
        log.info("Coordinator.statsJournalTable()  allJournals.size = ${allJournals.size}")
        val sorted = allJournals.map { JournalCount(it.key, it.value.size) }
            .sortedByDescending { it.numOfCitations }

        return JournalTableStats(allJournals.size, sorted.take(50))
    }

    /** Author Stats */
    fun authorYearsPublishedStats(): AuthorYearsPublised {
        val yearsPublishedGrouped: HashMap<Int, Int> = HashMap()
        val res = mongo.authors.aggregate<YearsPublishedResult>(
            match(Author::_id ne null),
            project(
                Author::papers from Author::papers /
                    PaperMetatdata::year
            )
        ).toList()
            .map { it.papers }
            .flatten()

        res.forEach { key ->
            val trimmed = key.trim().toIntOrNull() ?: 0
            yearsPublishedGrouped[trimmed]?.let {
                yearsPublishedGrouped.put(trimmed, it + 1)
            } ?: run {
                yearsPublishedGrouped.put(trimmed, 1)
            }
        }
        return AuthorYearsPublised(yearsPublishedGrouped)
    }

    fun firstNamesTableTotalNames(): Int {
        return mongo.firstNameTable.countDocuments().toInt()
    }

    fun statsGenderedAuthorsTable(): AuthorTableStats {
        return authorRepo.statsGenderedAuthorsTable()
    }

    fun statsAuthorTable(): AuthorYearsPublised {
        val yearsPublished = authorYearsPublishedStats()
        log.info("Coordinator.checkAuthorTable()  res = yearsPublished.yearsPublished.size" + yearsPublished.yearsPublishedCountByYear?.size)
        return yearsPublished
    }
}

