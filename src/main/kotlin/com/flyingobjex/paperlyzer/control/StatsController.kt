package com.flyingobjex.paperlyzer.control

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.PaperMetatdata
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.parser.CSVHelper
import com.flyingobjex.paperlyzer.parser.DisciplineType
import com.flyingobjex.paperlyzer.process.reports.AuthorReportLine
import com.flyingobjex.paperlyzer.process.reports.PaperReportLine
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.JournalTableRepo
import com.flyingobjex.paperlyzer.repo.toShortKeys
import com.flyingobjex.paperlyzer.util.CollectionUtils.Companion.withoutFirst
import com.flyingobjex.paperlyzer.util.CollectionUtils.Companion.withoutLast
import com.flyingobjex.paperlyzer.util.GenderUtils.toGenderRatio
import java.io.File
import java.text.NumberFormat
import java.util.*
import java.util.logging.Logger
import kotlin.streams.asSequence
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

        val genderRatio = toGenderRatio(toShortKeys(it.authors), it.authors.size)
        val genderRatioWithoutFirst = toGenderRatio(toShortKeys(withoutFirst(it.authors)), it.authors.size)
        val genderRatioWithoutLast = toGenderRatio(toShortKeys(withoutLast(it.authors)), it.authors.size)

        val lastAuthorGender = toShortKeys(it.authors).last().toString()

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
            gendersShortKey = it.authorGendersShortKey ?: "",
            firstAuthorGender = it.firstAuthorGender ?: "",
            lastAuthorGender = lastAuthorGender,
            genderCompletenessScore = it.genderCompletenessScore ?: -5.0,
            genders = toShortKeys(it.authors),
            genderCount = it.totalIdentifiableAuthors?.toLong() ?: -5,
            genderRatio = genderRatio,
            genderRatioWithoutFirst = genderRatioWithoutFirst,
            genderRatioWithoutLast = genderRatioWithoutLast,
            genderRatioOfCoAuthors = genderRatioWithoutFirst,
            totalAuthors = it.totalAuthors ?: -5,
            totalCoAuthors = (it.totalAuthors ?: -5) - 1,
            totalIdentifiableAuthors = it.totalIdentifiableAuthors ?: -5,
            citationsCount = it.citationsCount ?: -5,
            influentialCitationsCount = it.influentialCitationsCount ?: -5,
            discipline = it.discipline ?: DisciplineType.NA,
            discScore = it.score ?: -5,
            discTopic = it.matchingCriteria?.sortedByDescending { it.score }?.firstOrNull()?.term,
            originalTopics = it.topics.joinToString("; "),
            sjrRank = it.sjrRank ?: -5,
            hIndex = it.hIndex ?: -5,
        )
    }

    fun authorTableStats(){
        
    }

    /** Disciplines */
//    fun resetDisciplinesReport() {
//        mongo.reports.drop()
//    }
//
//    fun runPapersWithDisciplinesReport() {
//        log.info("StatsCoordinator.runPapersWithDisciplinesReport()  START!")
//        val asReportLines = papersToReportLines(emptyList())
//        log.info("StatsCoordinator.runPapersWithDisciplinesReport()  UPDATE!")
//        asReportLines.parallelStream().forEach {
//            mongo.reports.insertOne(it)
//        }
//        log.info("StatsCoordinator.runPapersWithDisciplinesReport()  DONE!")
//    }
//
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
                    firstName = author.firstName ?: "",
                    lastName = author.lastName,
                    gender = author.gender.gender,
                    genderProbability = author.gender.probability,
                    totalPapers = author.paperCount,
                    totalAsFirstAuthor = author.firstAuthorCount?.toInt() ?: 0,
                    yearsPublished = years.joinToString(";"),
                    firstYearPublished = years.firstOrNull() ?: 0,
                    lastYearPublished = years.lastOrNull() ?: 0,
                    publishedShortTitles = author.publishedShortTitles().joinToString(";"),
                    orcID = author.orcIDString,
                    coAuthorMean = author.averageCoAuthors ?: -5.5,
                    discipline = author.discipline ?: DisciplineType.NA,
                    disciplineScore = author.disciplineScore ?: -5.5,
                    sjrScores = "NA",
                    hIndexes = "NA"
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

