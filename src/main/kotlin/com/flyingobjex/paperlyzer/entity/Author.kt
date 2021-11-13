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
enum class GenderIdentitiy {
    FEMALE, MALE, NOFIRSTNAME, INITIALS, UNASSIGNED, NA;

    companion object {
        fun toType(value: String): GenderIdentitiy {
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

    fun toShortKey():String{
        return when(this){
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
    var genderIdt: GenderIdentitiy? = null,
    var probabilityStr: Double? = null,
    var firstYearPublished: Int? = null,
    var lastYearPublished: Int? = null,
    var ssFirstYearPublished:Int? = null,
    var ssLastYearPublished:Int? = null,
    var ssProcessedYearsPub:Boolean = false,
    val totalPapers:Int? = null,
    val averageCoAuthors: Double? = null,
    val unprocessed:Boolean? = false,
    val discipline:DisciplineType? = null,
    val disciplineScore:Double? = null,
    var _id: Id<Author>? = null,
){
    fun toYearsPublished(): List<String> = papers?.map { it.year.trim() } ?: emptyList()
    fun toYearsPublishedAsInt(): List<Int> = papers?.map { it.year.trim().toIntOrNull() ?: 0 } ?: emptyList()
    fun publishedTitles(): List<String> = papers?.map { it.title } ?: emptyList()
}

@Serializable
data class Gender(
    var gender: GenderIdentitiy,
    var probability: Double
) {
    companion object {
        val unassigned = Gender(GenderIdentitiy.UNASSIGNED, 0.0)
        val nofirstname = Gender(GenderIdentitiy.NOFIRSTNAME, 0.0)
        val initials = Gender(GenderIdentitiy.INITIALS, 0.0)
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
data class GenderDetails(
    val firstName: String,
    val genderIdentity: GenderIdentitiy,
    val probability: Double = -1.0,
    val apiResponse: GenderApiResponse,
    val apiRequest: GenderApiRequest,
    val _id: String? = null
)
