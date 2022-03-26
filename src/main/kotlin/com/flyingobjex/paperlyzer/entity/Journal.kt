package com.flyingobjex.paperlyzer.entity

import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

enum class JournalTextType {
    Article, Review, ProceedingsPaper, EditorialMaterial, NA;

    fun toType(value: String): JournalTextType? =
        when (value) {
            "Article" -> Article
            "Review" -> Review
            "ProceedingsPaper" -> ProceedingsPaper
            "EditorialMaterial" -> EditorialMaterial
            "NA" -> NA
            else -> null
        }
}

enum class JournalScienceType {
    Stem, SocialScience, NA;

    fun toType(value: String): JournalScienceType? =
        when (value) {
            "Stem" -> Stem
            "SocialScience" -> SocialScience
            "NA" -> NA
            else -> null
        }
}

enum class ReviewPolicy {
    MandatoryBlind,
    OptionalBlind,
    NotBlind,
    NotPeerReviewed,
    Blank,
    NA;

    // Issues in Science & Technology
    // ISSUES IN SCIENCE & TECHNOLOGY
    companion object {

        fun toType(value: String): ReviewPolicy? =
            when (value) {
                "mandatory blind" -> MandatoryBlind
                "optional blind" -> OptionalBlind
                "not blind" -> NotBlind
                "not peer reviewed" -> NotPeerReviewed
                "NA" -> NA
                "" -> Blank
                else -> throw Error("review policy type not found $value")
            }
    }
}

class Journal(
    val journalName: String,
    val textType: JournalTextType,
    val doi: String,
    var shortTitles: List<String>,
    var citationCount: Int,
    val keywords: List<String>? = null,
    val topics: List<String>,
    val impactScore: Double? = null,
    val journalScienceType: JournalScienceType? = null,
    val reviewPolicy: ReviewPolicy? = null,
    val _id: Id<String>? = null,
)

@Serializable
data class ReviewPolicyResponse(
    private val coder: String,
    private val editorEmailed: String,
    private val frequency: String,
    private val journal: String,
    private val notes: String,
    private val personWhoResponded: String,
    private val reviewPolicy: String,
    private val secondEditorEmailed: String,
    private val sinceWhen: String
) {
    val policy = ReviewPolicy.toType(reviewPolicy)
    val title = journal.uppercase()
    val frequecyAsInt = frequency.toIntOrNull() ?: -5
}


class ReviewPolicyService(private val policies: List<ReviewPolicyResponse>) {

    fun applyPolicy(journalTitle: String): ReviewPolicy {
        policies
            .filter { it.title == journalTitle }
            .maxByOrNull { it.frequecyAsInt }?.let { journal ->
                journal.policy?.let {
                    return it
                }
            }
        return ReviewPolicy.NA
    }


}
