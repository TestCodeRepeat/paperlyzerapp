package com.flyingobjex.paperlyzer.entity

import kotlinx.serialization.Serializable
import org.litote.kmongo.Id


@Serializable
data class SemanticScholarPaper(
    val abstract: String? = null,
    val arxivId: String? = null,
    val authors: List<SsAuthorDetails>? = null,
    val citationVelocity: Int? = null,
    val citations: List<Citation>? = null,
    val corpusId: Int? = null,
    val doi: String? = null,
    val fieldsOfStudy: List<String>? = null,
    val influentialCitationCount: Int? = null,
    val isOpenAccess: Boolean? = null,
    val isPublisherLicensed: Boolean? = null,
    val is_open_access: Boolean? = null,
    val is_publisher_licensed: Boolean? = null,
    val numCitedBy: Int? = null,
    val numCiting: Int? = null,
    val paperId: String? = null,
    val references: List<Reference>? = null,
    val title: String? = null,
    val topics: List<Topic>? = null,
    val url: String? = null,
    val venue: String? = null,
    val year: Int? = null,
    val wosDoi: String? = null,
    val _id: String? = null,
)


@Serializable
data class SsAuthorDetails(
    val authorId: String? = null,
    val name: String? = null,
    val url: String? = null,
)

@Serializable
data class Citation(
    val arxivId: String? = null,
    val authors: List<AuthorSs>? = null,
    val doi: String? = null,
    val intent: List<String>? = null,
    val isInfluential: Boolean? = null,
    val paperId: String? = null,
    val title: String? = null,
    val url: String? = null,
    val venue: String? = null,
    val year: Int? = null
)

@Serializable
data class Reference(
    val arxivId: String? = null,
    val authors: List<AuthorSs>? = null,
    val doi: String? = null,
    val intent: List<String>? = null,
    val isInfluential: Boolean? = null,
    val paperId: String? = null,
    val title: String? = null,
    val url: String? = null,
    val venue: String? = null,
    val year: Int? = null,
)

@Serializable
data class Topic(
    val topic: String? = null,
    val topicId: String? = null,
    val url: String? = null,
)

@Serializable
data class AuthorSs(
    val authorId: String? = null,
    val name: String? = null,
)

@Serializable
data class SemanticScholarAuthor(
    val aliases: List<String>? = null,
    val authorId: String? = null,
    val hIndex: Int? = null,
    val papers: List<SsPaperSummary>? = null,
    val name: String,
    val firstName: String = "uninitialized",
    val lastName: String = "uninitialized",
    val middleName: String = "uninitialized",
    val firstNameProcessed: Boolean? = null,
    var _id: Id<SemanticScholarAuthor>? = null,
)

@Serializable
data class SsPaperSummary(
    val citationCount: Int? = null,
    val title: String? = null,
    val fieldsOfStudy: List<String>? = null,
    val influentialCitationCount: Int? = null,
    val paperId: String? = null,
    val year: Int? = null,
)
