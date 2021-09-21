package com.flyingobjex.paperlyzer.entity

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
    Blind,
    Open,
    Mixed,
    NA;

    fun toType(value: String): ReviewPolicy? =
        when (value) {
            "Blind" -> Blind
            "Open" -> Open
            "Mixed" -> Mixed
            "NA" -> NA
            else -> null
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
    val impactScore:Double? = null,
    val journalScienceType: JournalScienceType? = null,
    val reviewPolicy:ReviewPolicy? = null,
    val _id: Id<String>? = null,
)