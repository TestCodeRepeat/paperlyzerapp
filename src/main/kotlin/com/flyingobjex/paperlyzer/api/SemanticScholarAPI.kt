package com.flyingobjex.paperlyzer.api

import com.flyingobjex.paperlyzer.WsBroadcaster
import com.flyingobjex.paperlyzer.entity.SemanticScholarAuthor
import com.flyingobjex.paperlyzer.entity.SemanticScholarPaper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*

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

    suspend fun authorById(authorId: String): SemanticScholarAuthor? {
        val authorUrl =
            "https://api.semanticscholar.org/graph/v1/author/$authorId?fields=aliases,papers,papers.year,papers.citationCount,papers.influentialCitationCount,hIndex,papers.fieldsOfStudy,papers.title"

        try {
            fetchCount += 1
            val res = client.request<SemanticScholarAuthor>(authorUrl) {
                header("x-api-key", apiKey)
            }
            val mesB =
                "SemanticScholarAPI.kt :: SemanticScholarAPI :: success = $count :: fetechCount = $fetchCount :: $authorId ::"
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
