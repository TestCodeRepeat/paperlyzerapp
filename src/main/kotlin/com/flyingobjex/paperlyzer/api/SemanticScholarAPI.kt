package com.flyingobjex.paperlyzer.api

import com.flyingobjex.paperlyzer.WsBroadcaster
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

val SEMANTIC_SCHOLAR_API_KEY: String = System.getenv("SEMANTIC_SCHOLAR_API_KEY").toString()

class SemanticScholarAPI(semanticScholarApiKey: String) {

    private val apiKey = semanticScholarApiKey
    private val baseUrl = "https://api.semanticscholar.org/v1/paper"

    private var count = 0
    private var fetchCount = 0

    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun fetchSsPaperByDoi(doi: String): SemanticScholarPaper? {
        try {
            fetchCount += 1
            val res = client.request<SemanticScholarPaper>("$baseUrl/$doi") {
                header("x-api-key", apiKey)
            }
            val mesB =
                "SemanticScholarAPI.kt :: SemanticScholarAPI :: success = $count :: fetechCount = $fetchCount :: $doi ::"
            println(mesB)
            count += 1
            return res

        } catch (e: Exception) {
            val er = "!!!! SemanticScholarAPI.kt :: SemanticScholarAPI :: e = $e"
            println(er)
            WsBroadcaster.broadcast(er)
            return null
        }
    }
}

@Serializable
data class SemanticScholarPaper(
    val abstract: String? = null,
    val arxivId: String? = null,
    val authors: List<Author>? = null,
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
data class Author(
    val authorId: String? = null,
    val name: String? = null,
    val url: String? = null
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

