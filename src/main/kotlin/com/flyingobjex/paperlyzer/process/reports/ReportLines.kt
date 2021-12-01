package com.flyingobjex.paperlyzer.process.reports

import com.flyingobjex.paperlyzer.entity.GenderIdentitiy
import com.flyingobjex.paperlyzer.parser.DisciplineType
import kotlinx.serialization.Serializable

@Serializable
data class AuthorReportLine(
    val firstName: String,
    val lastName: String,
    val gender: GenderIdentitiy,
    val genderProbability: Double,
    val totalPapers: Int,
    val totalAsFirstAuthor: Int,
    val yearsPublished: String,
    val firstYearPublished: Int,
    val lastYearPublished: Int,
    val publishedShortTitles: String,
    val orcID: String?,
    val coAuthorMean: Double,
    val discipline: DisciplineType,
    val disciplineScore: Double,
    val sjrScores: String,
    val hIndexes: String,
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
    val originalTopics: String,
    var gendersShortKey: String,
    var firstAuthorGender: String,
    var lastAuthorGender: String,
    var genderCompletenessScore: Double,
    var genders:String,
    var genderCount: Long,
    var genderRatio: Double,
    var genderRatioWithoutFirst: Double,
    var genderRatioWithoutLast: Double,
    var genderRatioOfCoAuthors: Double,
    var totalAuthors: Int,
    var totalCoAuthors: Int,
    var totalIdentifiableAuthors: Int,
    var citationsCount: Int,
    var influentialCitationsCount: Int,
    var discipline: DisciplineType,
    var discScore: Int,
    val discTopic: String?,
    val sjrRank: Int,
    val hIndex: Int,
)
