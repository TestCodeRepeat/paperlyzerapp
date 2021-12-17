package com.flyingobjex.paperlyzer.entity

import com.flyingobjex.paperlyzer.api.GenderApiRequest
import com.flyingobjex.paperlyzer.api.GenderApiResponse
import com.flyingobjex.paperlyzer.parser.DisciplineType
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

/**
 * UNASSIGNED is the initial value
 * UNDETERMINED - no first name or initials are present
 * INITIALS - uses initials for first name
 * NA - not possible to derive any information
 */
enum class GenderIdentity {
    FEMALE, MALE, NOFIRSTNAME, INITIALS, UNASSIGNED, NA;

    companion object {
        fun toType(value: String): GenderIdentity {
            return when (value) {
                "female" -> FEMALE
                "male" -> MALE
                "NOFIRSTNAME" -> NOFIRSTNAME
                "INITIALS" -> INITIALS
                "UNASSIGNED" -> UNASSIGNED
                "NA" -> NA
                else -> UNASSIGNED
            }
        }
    }

    fun toShortKey(): String {
        return when (this) {
            FEMALE -> "F"
            MALE -> "M"
            NOFIRSTNAME -> "Y"
            INITIALS -> "Z"
            UNASSIGNED -> "X"
            NA -> "X"
        }
    }
}

@Serializable
data class Author(
    val lastName: String,
    val firstName: String? = null,
    val middleName: String? = null,
    var gender: Gender,
    var papers: MutableList<PaperMetatdata>? = null,
    val duplicateCheck: Boolean = false,
    var paperCount: Int = 1,
    val orcID: OrcID? = null,
    val orcIDString: String? = null,
    var genderIdt: GenderIdentity? = null,
    var probabilityStr: Double? = null,
    var firstYearPublished: Int? = null,
    var lastYearPublished: Int? = null,
    var ssFirstYearPublished: Int? = null,
    var ssLastYearPublished: Int? = null,
    var ssProcessedYearsPub: Boolean = false,
    val totalPapers: Int? = null,
    val averageCoAuthors: Double? = null,
    val firstAuthorCount: Long? = null,
    val authorReportUnprocessed: Boolean? = false,
    val disciplineScore: Double? = null,
    val discipline: DisciplineType? = null,
    val averageGenderRatioOfPapers: Double? = null,
    val genderRatioOfAllCoAuthors: Double? = null,
    var yearsPublished:String? = null,
    var _id: Id<Author>? = null,
) {

    fun toYearsPublished(): List<String> = papers?.map { it.year.trim() } ?: emptyList()
    fun toYearsPublishedAsInt(): List<Int> = papers?.map { it.year.trim().toIntOrNull() ?: 0 } ?: emptyList()
    fun publishedTitles(): List<String> = papers?.map { it.title } ?: emptyList()
    fun publishedShortTitles(): List<String> = papers?.map { it.shortTitle } ?: emptyList()
}

fun Author.hasNullPublishedYear(): Boolean {
    return (firstYearPublished != null && firstYearPublished ?: 0 == 0) || (lastYearPublished != null && lastYearPublished ?: 0 == 0)
}

fun Author.applyFirstLastYearsPublished(): Pair<Int, Int> {
    val sorted = toYearsPublishedAsInt().sorted()
    val first = sorted.firstOrNull() ?: 0
    val last = sorted.lastOrNull() ?: 0
    this.firstYearPublished = first
    this.lastYearPublished = last
    this.yearsPublished = sorted.joinToString(",")
    return Pair(first, last)
}


@Serializable
data class Gender(
    var gender: GenderIdentity,
    var probability: Double
) {
    companion object {
        val unassigned = Gender(GenderIdentity.UNASSIGNED, 0.0)
        val nofirstname = Gender(GenderIdentity.NOFIRSTNAME, 0.0)
        val initials = Gender(GenderIdentity.INITIALS, 0.0)
    }
}

@Serializable
data class OrcID(
    val id: String,
    val lastName: String,
    val firstName: String,
    val middleName: String? = null
)

@Serializable
data class GenderedNameDetails(
    val firstName: String,
    val genderIdentity: GenderIdentity,
    val probability: Double = -1.0,
    val apiResponse: GenderApiResponse,
    val apiRequest: GenderApiRequest,
    val _id: String? = null
)
