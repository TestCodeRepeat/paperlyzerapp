package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.*
import com.flyingobjex.paperlyzer.parser.DisciplineType
import com.flyingobjex.paperlyzer.parser.MatchingCriteria
import com.flyingobjex.paperlyzer.process.DisciplineProcessStats
import com.flyingobjex.paperlyzer.process.reports.ReportStats
import com.flyingobjex.paperlyzer.process.SJRStats
import com.flyingobjex.paperlyzer.process.WosCitationStats
import com.mongodb.client.result.UpdateResult
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlinx.serialization.Serializable
import org.litote.kmongo.*

data class AuthorResult(val _id: String, val shortTitle: String, val authors: List<Author>)


fun matchGender(name: String?, genderDetails: List<GenderDetails>): GenderDetails? =
    genderDetails.firstOrNull { it.firstName == name }

class WoSPaperRepository(val mongo: Mongo, val logMessage: ((message: String) -> Unit)? = null) {

    val log: Logger = Logger.getAnonymousLogger()

    /** Reports */
    fun resetReportLines() {
        mongo.reports.drop()
        mongo.genderedPapers.updateMany(
            WosPaper::reported ne false,
            setValue(WosPaper::reported, false)
        )
    }

    fun getReportStats(): ReportStats {
        return ReportStats(
            totalReportsProcessed = mongo.genderedPapers.countDocuments(WosPaper::reported eq true).toInt(),
            totalUnprocessed = mongo.genderedPapers.countDocuments(WosPaper::reported ne true).toInt(),
        )
    }

    /** Discipline & Topics  */
    fun resetDisciplineProcessed() {
        mongo.genderedPapers.updateMany(
            WosPaper::_id ne null,
            listOf(
                setValue(WosPaper::discipline, null),
                setValue(WosPaper::score, -5),
                setValue(WosPaper::matchingCriteria, null),
                setValue(WosPaper::topSSH, null),
                setValue(WosPaper::topStem, null),
            )
        )

    }

    fun quickResetDisciplineProcessed() {
        mongo.genderedPapers.updateMany(
            WosPaper::discipline ne null,
            listOf(
                setValue(WosPaper::discipline, null),
                setValue(WosPaper::score, null),
                setValue(WosPaper::matchingCriteria, null),
                setValue(WosPaper::topSSH, null),
                setValue(WosPaper::topStem, null),
            )
        )

    }

    fun getDisciplineStats(): DisciplineProcessStats {
        return DisciplineProcessStats(
            totalProcessedWithDiscipline = mongo.genderedPapers.countDocuments(WosPaper::discipline ne null).toInt(),
            totalUnprocessed = mongo.genderedPapers.countDocuments(WosPaper::discipline eq null).toInt(),
            totalStem = mongo.genderedPapers.countDocuments(WosPaper::discipline eq DisciplineType.STEM).toInt(),
            totalSSH = mongo.genderedPapers.countDocuments(WosPaper::discipline eq DisciplineType.SSH).toInt(),
            totalMaybe = mongo.genderedPapers.countDocuments(WosPaper::discipline eq DisciplineType.M).toInt(),
            totalUnidentified = mongo.genderedPapers.countDocuments(WosPaper::discipline eq DisciplineType.NA).toInt(),
            totalWosPapers = mongo.genderedPapers.countDocuments().toInt(),
        )
    }

    fun getSJRStats(): SJRStats {

        val totalPapers = mongo.genderedPapers.countDocuments().toInt()
        val processedCount = mongo.genderedPapers.countDocuments(
            and(
                WosPaper::sjrRank gt -5,
                WosPaper::sjrRank ne null,
            )
        ).toInt()

        return SJRStats(
            totalProcessedWithSJRIndex = processedCount,
            totalWithMatchingSJR = mongo.genderedPapers.countDocuments(WosPaper::sjrRank gt 0).toInt(),
            totalUnidentified = mongo.genderedPapers.countDocuments(WosPaper::sjrRank eq 0).toInt(),
            totalUnprocessed = totalPapers - processedCount,
            totalWosPapers = totalPapers
        )
    }

    fun applyMatchingCriteria(paper: WosPaper, allCriteria: List<MatchingCriteria>): WosPaper {
        val topCriteria = allCriteria.sortedByDescending { it.score }.firstOrNull()

        val discipline = topCriteria?.topic?.disciplineType ?: DisciplineType.NA

        val topSTEM = allCriteria.filter { it.topic.disciplineType == DisciplineType.STEM }
            .sortedByDescending { it.score }

        val topSSH = allCriteria.filter { it.topic.disciplineType == DisciplineType.SSH }
            .sortedByDescending { it.score }

        return paper.copy(
            matchingCriteria = allCriteria,
            discipline = discipline,
            score = topCriteria?.score ?: 0,
            topStem = topSTEM.firstOrNull(),
            topSSH = topSSH.firstOrNull(),
        )
    }

    fun getUnprocessedPapersByDiscipline(batchSize: Int): List<WosPaper> =
        mongo.genderedPapers.aggregate<WosPaper>(
            match(WosPaper::discipline eq null),
            limit(batchSize),
        ).toList()

    fun updatePaperDiscipline(updated: WosPaper) {
        mongo.genderedPapers.updateOne(
            WosPaper::_id eq updated._id,
            listOf(
                setValue(WosPaper::discipline, updated.discipline),
                setValue(WosPaper::score, updated.score),
                setValue(WosPaper::matchingCriteria, updated.matchingCriteria),
                setValue(WosPaper::topStem, updated.topStem),
                setValue(WosPaper::topSSH, updated.topSSH),
            )
        )
    }

    fun unprocessedDisciplinesCount(): Long =
        mongo.genderedPapers.countDocuments(WosPaper::score eq -5)

    fun unprocessedSJRIndexCount(): Long =
        mongo.genderedPapers.countDocuments(WosPaper::sjrRank eq -5)

    /** Citations */
    fun updateCitationsCount(wosPaperId: WosPaperId, count: Int, influentialCount: Int): UpdateResult =
        mongo.genderedPapers.updateOne(
            WosPaper::_id eq wosPaperId._id,
            listOf(
                setValue(WosPaper::citationsCount, count),
                setValue(WosPaper::influentialCitationsCount, influentialCount),
                setValue(WosPaper::citationsProcessed, true),
            )
        )

    fun getUnprocessedByCitations(batchSize: Int): List<WosPaperId> =
        mongo.genderedPapers.aggregate<WosPaperId>(
            match(WosPaper::citationsProcessed eq false),
            project(WosPaperId::doi from WosPaper::doi),
            limit(batchSize),
        ).toList()

    fun getUnprocessedPapersAsPaperIds(batchSize: Int): List<WosPaperId> {
        val res = mongo.genderedPapers.aggregate<WosPaperId>(
            match(
                or(
                    WosPaper::ssProcessed eq null,
                    WosPaper::ssProcessed ne true,
                ),
                and(
                    WosPaper::ssFailed ne true,
                    WosPaper::doi ne "NA"
                )
            ),
            project(WosPaperId::doi from WosPaper::doi),
            limit(batchSize)
        ).toList()

        return res
    }

    fun unprocessedCitationsCount(): Int =
        mongo.genderedPapers.countDocuments(
            WosPaper::citationsProcessed eq false
        ).toInt()

    fun processedCitationsCount(): Int =
        mongo.genderedPapers.countDocuments(
            WosPaper::citationsProcessed eq true
        ).toInt()

    fun resetSJRIndexProcessed() {
        log.info("WoSPaperRepository.resetSJRIndexProcessed()  ")
        logMessage?.let { it("WoSPaperRepository.resetSJRIndexProcessed()  ") }
        val time = measureTimeMillis {
            mongo.genderedPapers.updateMany(
                or(WosPaper::sjrRank ne -5),
                listOf(
                    setValue(WosPaper::sjrRank, -5),
                    setValue(WosPaper::hIndex, -5),
                )
            )
        }
        log.info("WoSPaperRepository.resetSJRIndexProcessed()  COMPLETED in $time ms")
        logMessage?.let { it("WoSPaperRepository.resetSJRIndexProcessed() COMPLETED in $time ms  ") }
    }


    fun resetCitationProcessed(): String {
        log.info("WoSPaperRepository.resetCitationProcessed()  ")
        logMessage?.let { it("WoSPaperRepository.resetCitationProcessed()  ") }
        mongo.genderedPapers.updateMany(
            WosPaper::citationsProcessed ne false,
            listOf(
                setValue(WosPaper::citationsProcessed, false),
                setValue(WosPaper::influentialCitationsCount, 0),
                setValue(WosPaper::citationsCount, 0),
            )
        )

        logMessage?.let { it("WoSPaperRepository.resetCitationProcessed()  complete") }
        return "done"

    }

    /** Semantic Scholar  */

    fun resetSsProcessed() {
        log.info("WoSPaperRepository.resetSsProcessed()  ")
        mongo.genderedPapers.updateMany(
            WosPaper::ssProcessed eq true,
            listOf(
                setValue(WosPaper::ssProcessed, false),
                setValue(WosPaper::ssFailed, null)
            )
        )

        mongo.genderedPapers.updateMany(
            WosPaper::ssFailed ne null,
            setValue(WosPaper::ssFailed, null)
        )
    }

    fun markSsAsFailedById(paperId: WosPaperId): UpdateResult {
        return mongo.genderedPapers.updateOne(
            WosPaper::_id eq paperId._id,
            listOf(setValue(WosPaper::ssFailed, true), setValue(WosPaper::ssProcessed, true)),
        )
    }

    fun markSsAsProcessedById(wosPaper: WosPaperId): UpdateResult {
        return mongo.genderedPapers.updateOne(
            WosPaper::_id eq wosPaper._id,
            setValue(WosPaper::ssProcessed, true)
        )
    }

    fun applyGenderToPaperAuthors(papers: List<WosPaper>) {
        papers.parallelStream().forEach { paper ->
            val viableAuthors = paper.authors.filter { it.gender.gender == GenderIdentitiy.UNASSIGNED }

            val allGenderShortkeys = toShortKeys(paper.authors)
            val withoutFirstAuthor =
                if (allGenderShortkeys.length > 1)
                    allGenderShortkeys.subSequence(1, allGenderShortkeys.length - 1)
                else "-"

            if (viableAuthors.isNotEmpty()) {
                val matches = mongo.genderTable.find(
                    GenderDetails::firstName `in` paper.authors.mapNotNull { it.firstName },
                ).toList()

                paper.authors.forEach { author ->
                    val match = matchGender(author.firstName, matches)
                    author.genderIdt = match?.genderIdentity
                    author.gender = Gender(match?.genderIdentity ?: GenderIdentitiy.NA, match?.probability ?: 0.0)
                }
                paper.authorGendersShortKey = allGenderShortkeys

                paper.firstAuthorGender = paper.authors.firstOrNull()?.gender?.gender?.toShortKey()
                paper.withoutFirstAuthorGender = withoutFirstAuthor.toString()

                val totalAuthors = paper.authors.size
                paper.totalAuthors = totalAuthors
                val identifiableAuthors = paper.authors
                    .filter {
                        it.gender.gender == GenderIdentitiy.MALE || it.gender.gender == GenderIdentitiy.FEMALE
                    }
                    .size
                paper.totalIdentifiableAuthors = identifiableAuthors
                val genderCompletenessScore = identifiableAuthors.toDouble() / totalAuthors.toDouble()
                paper.genderCompletenessScore = genderCompletenessScore

                if (genderCompletenessScore == 1.0){
                    paper.genderCompletenessScore
                }

                mongo.genderedPapers.insertOne(paper)
                mongo.rawPaperFullDetails.updateOne(
                    WosPaper::_id eq paper._id,
                    setValue(WosPaper::processed, true)
                )

            } else {
                mongo.genderedPapers.insertOne(paper)
                mongo.rawPaperFullDetails.updateOne(
                    WosPaper::_id eq paper._id,
                    setValue(WosPaper::processed, true)
                )
            }
        }


    }

    fun getPapersWithAuthors(batchSize: Int): List<WosPaper> {
        return mongo.rawPaperFullDetails.aggregate<WosPaper>(
            match(
                or(
                    WosPaper::processed eq false,
                    WosPaper::processed eq null
                )
            )
        ).take(batchSize).toList()
    }

    fun resetPaperTableGenderInfo() {
        mongo.genderedPapers.drop()

        mongo.rawPaperFullDetails.updateMany(
            WosPaper::processed ne false,
            listOf(
                setValue(WosPaper::processed, false),
            )
        )

        mongo.resetIndexes()
    }

    fun getAllRawAuthors(): List<Author> {
        val res = mongo.rawPaperFullDetails.aggregate<AuthorResult>(
            match(WosPaper::shortTitle ne null),
            project(
                WosPaper::shortTitle from WosPaper::shortTitle,
                WosPaper::authors from WosPaper::authors
            )
        ).toList()

        return res.map { it.authors }.flatten()
    }

    fun insertRawCsvPapers(rawCsvPapers: List<WosPaper>) {
        mongo.rawPaperFullDetails.insertMany(rawCsvPapers)
    }

    fun getAllRawPapers(): List<WosPaper> {
        return mongo.rawPaperFullDetails.find().toList()
    }

    fun clearPapers() {
        mongo.clearPapers()
    }

    fun getCitationStats(): WosCitationStats {
        return WosCitationStats(
            totalProcessedWosPapers = mongo.genderedPapers.countDocuments(
                and(
                    WosPaper::citationsProcessed eq true,
                    WosPaper::citationsCount gte 0
                )
            ).toInt(),
            totalUnprocessedWosPapers = mongo.genderedPapers.countDocuments(WosPaper::citationsProcessed eq false)
                .toInt(),
            totalFailedWosPapers = mongo.genderedPapers.countDocuments(
                and(
                    WosPaper::citationsCount eq -5,
                    WosPaper::citationsProcessed eq true
                )
            ).toInt(),
            totalSsPapers = mongo.ssPapers.countDocuments().toInt(),
            totalWosPapers = mongo.genderedPapers.countDocuments().toInt(),
        )
    }

    fun getPapersWithStemSsh(shortTitles: List<String>): List<WosPaperWithStemSsh> {

        val res = mongo.genderedPapers.aggregate<WosPaperWithStemSsh>(
            match(WosPaper::shortTitle `in` shortTitles),
            project(
                WosPaperWithStemSsh::_id from WosPaper::_id,
                WosPaperWithStemSsh::shortTitle from WosPaper::shortTitle,
                WosPaperWithStemSsh::discipline from WosPaper::discipline
            ),
        ).toList()

        return res
    }

    fun getPapers(shortTitles: List<String>): List<WosPaperWithAuthors> {

        return mongo.genderedPapers.aggregate<WosPaperWithAuthors>(
            match(WosPaper::shortTitle `in` shortTitles),
            project(
                WosPaperWithAuthors::_id from WosPaper::_id,
                WosPaperWithAuthors::doi from WosPaper::doi,
                WosPaperWithAuthors::totalAuthors from WosPaper::totalAuthors,
                WosPaperWithAuthors::authors from WosPaper::authors,
            ),
        ).toList()

    }

    fun getPapersWithCoAuthors(shortTitles: List<String>): List<WosPaperWithAuthors> {

        return mongo.genderedPapers.aggregate<WosPaperWithAuthors>(
            match(WosPaper::shortTitle `in` shortTitles),
            project(
                WosPaperWithAuthors::_id from WosPaper::_id,
                WosPaperWithAuthors::doi from WosPaper::doi,
                WosPaperWithAuthors::totalAuthors from WosPaper::totalAuthors,
                WosPaperWithAuthors::authors from WosPaper::authors,

            ),
        ).toList()

    }

    fun getPaperReduced(id: String): WosPaperWithAuthors? =
        mongo.genderedPapers.aggregate<WosPaperWithAuthors>(
            match(WosPaper::doi eq id),
            project(
                WosPaperWithAuthors::_id from WosPaper::_id,
                WosPaperWithAuthors::doi from WosPaper::doi,
                WosPaperWithAuthors::totalAuthors from WosPaper::totalAuthors
            ),
        ).toList().firstOrNull()

    fun getPaper(id: String): WosPaper? =
        mongo.genderedPapers.findOne(WosPaper::_id eq id)

    fun getUnprocessedPapersBySJRIndex(batchSize: Int): List<WosPaper> =
        mongo.genderedPapers.aggregate<WosPaper>(
            match(
                or(WosPaper::sjrRank eq -5),
            ),
            limit(batchSize)
        ).toList()
}

data class WosPaperReduced(val doi: String)

fun toShortKeys(authors: List<Author>): String =
    authors.joinToString("") { it.genderIdt?.toShortKey() ?: "X" }
