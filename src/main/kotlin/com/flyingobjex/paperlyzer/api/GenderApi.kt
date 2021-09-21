package com.flyingobjex.paperlyzer.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

val GENDER_API_KEY: String = System.getenv("GENDER_API_KEY").toString()

class GenderApi(private val apiKey: String) {

    private val baseUrlV2 = "https://gender-api.com/v2/gender"
    private val baseUrlV1 = "https://gender-api.com/get"

    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun fetchGenderByFullName(firstName: String, lastName: String): GenderApiV1FullNameReponse =
        client.request("$baseUrlV1?split=$firstName%20$lastName&key=$apiKey")


    suspend fun fetchGenderMultipleFirstNames(requests: List<GenderApiRequest>): List<GenderApiResponse> =
        client.post(baseUrlV2) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, apiKey)
            body = Json.encodeToJsonElement(requests.toTypedArray())
        }


    suspend fun fetchGenderByFirstname(firstName: String): GenderApiResponse =
        client.post(baseUrlV2) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, apiKey)
            body = GenderApiRequest(firstName)
        }


    suspend fun fetchGender(fullName: String): GenderApiResponse =
        client.post(baseUrlV2) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, apiKey)
            body = GenderApiFullNameRequest(fullName)
        }


}

@Serializable
data class GenderApiFullNameRequest(val full_name: String)

@Serializable
data class GenderApiRequest(val first_name: String)

@Serializable
data class GenderApiResponse(
    val details: Details? = null,
    val first_name: String? = null,
    val gender: String? = null,
    val input: Input? = null,
    val last_name: String? = null,
    val probability: Double? = null,
    val result_found: Boolean? = null
)

@Serializable
data class Details(
    val country: String? = null,
    val credits_used: Int? = null,
    val duration: String? = null,
    val first_name_sanitized: String? = null,
    val samples: Int? = null
)

@Serializable
data class Input(
    val full_name: String? = null,
)

@Serializable
data class GenderApiV1FirstNameReponse(
    val accuracy: Int?,
    val country: String?,
    val credits_used: Int?,
    val duration: String?,
    val gender: String?,
    val name: String?,
    val name_sanitized: String?,
    val samples: Int?
)

@Serializable
data class GenderApiV1FullNameReponse(
    val accuracy: Int,
    val country: String,
    val credits_used: Int,
    val duration: String,
    val first_name: String,
    val gender: String,
    val last_name: String,
    val name: String,
    val name_sanitized: String,
    val samples: Int,
    val strict: Boolean
)
