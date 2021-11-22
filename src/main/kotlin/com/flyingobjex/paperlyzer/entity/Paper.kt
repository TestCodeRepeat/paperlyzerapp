package com.flyingobjex.paperlyzer.entity

import com.flyingobjex.paperlyzer.api.SemanticScholarPaper
import com.flyingobjex.paperlyzer.parser.Discipline
import com.flyingobjex.paperlyzer.parser.DisciplineType
import com.flyingobjex.paperlyzer.parser.MatchingCriteria
import com.flyingobjex.paperlyzer.parser.PLTopic
import com.flyingobjex.paperlyzer.repo.IWosPaperWithStemSsh
import kotlinx.serialization.Serializable

data class LineItem(val key: String? = null, val body: String)


@Serializable
data class CombinedPaper(
    val wosPaper: WosPaper,
    val ssPaper: SemanticScholarPaper? = null
)


@Serializable
data class RawCsvPaper(
    val shortTitle: String,
    val authors: String,
    val year: String,
    val title: String,
    val journal: String,
    val text_type: String,
    val keywords: String,
    val emails: String,
    val orcid: String,
    val doi: String,
    val topic: String,
    val processed: Boolean = false,
    val _id: String? = null,
)

@Serializable
data class PaperMetatdata(
    val shortTitle: String,
    val year: String,
    val title: String,
    val journal: String,
    val orcidRaw: String,
    val ordIds: List<OrcID>? = emptyList(),
    val doi: String,
    val topics: List<String>,
    val originalAuthors: String,
    val authorGenders: List<GenderIdentitiy>? = null,
    val authorGenderTypes: List<GenderIdentitiy>? = null,
    val firstAuthorGender: GenderIdentitiy? = null,
    val _id: String? = null,
)

@Serializable
data class WosPaper(
    override val shortTitle: String,
    val authors: List<Author>,
    val year: String,
    val title: String,
    val journal: String,
    val text_type: String,
    val keywords: String,
    val emails: String,
    val orcid: String,
    val ordIds: List<OrcID>? = emptyList(),
    val doi: String,
    val topics: List<String>,
    val processed: Boolean? = false,
    var authorGendersShortKey: String? = null,
    var firstAuthorGender: String? = null,
    var withoutFirstAuthorGender: String? = null,
    var genderCompletenessScore: Double? = null,
    var totalAuthors: Int? = null,
    var totalIdentifiableAuthors: Int? = null,
    var citationsCount: Int? = null,
    var influentialCitationsCount: Int? = null,
    var ssProcessed: Boolean = false,
    var ssFailed: Boolean? = null,
    var citationsProcessed: Boolean? = null,
    override var discipline: DisciplineType? = null,
    var score: Int? = null,
    var matchingCriteria: List<MatchingCriteria>? = null,
    var topStem: MatchingCriteria? = null,
    var topSSH: MatchingCriteria? = null,
    var reported: Boolean? = null,
    var sjrRank: Int? = null,
    var hIndex: Int? = null,
    override val _id: String? = null,
) : IWosPaperWithStemSsh

private fun String.clean(): String {
    return trim()
        .replace("-", " ")
        .toLowerCase()
}

fun PaperMetatdata.orcidForNames(lastNameRaw: String, firstNameRaw: String): OrcID? {
    if (ordIds == null || ordIds.isEmpty()) return null
    val lastName = lastNameRaw.clean()
    val firstName = firstNameRaw.clean()

    return ordIds.firstOrNull {
        lastName == it.lastName.clean() &&
            (firstName == it.firstName.clean())
    }
}

fun hasTwoDots(value: String): Boolean = value
    .filter { it.toString() == "." }
    .count() > 1

fun isAllCaps(value: String): Boolean {
    value.forEach { if (it.isLowerCase()) return false }
    return true
}

