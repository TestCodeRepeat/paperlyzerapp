package com.flyingobjex.paperlyzer.parser

import com.flyingobjex.paperlyzer.BASE_URL
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


data class HIndexLine(val title:String, val sjr: Int, val hIndex:Int )

fun cleanTitle(title:String):String{
    return title
        .lowercase()
        .replace("&amp;", "")
        .replace("&amp;", "&")
        .replace(", the", "")
        .trim()
}


class HIndexModel(){

    var hIndexData:List<HIndexLine> = emptyList()

    suspend fun loadHindexData(): Any {
        val url = "$BASE_URL/static/hindex.csv"
        return client.request<Any>(url)
    }

    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }



}


@Serializable
data class SJRJournal(
    val country: String,
    val hIndex: Long,
    val issn: String,
    val publisher: String,
    val rank: Long,
    val region: String,
    val sjr: String,
    val sourceid: Long,
    val title: String,
    val type: String
)

object HIndexParser {

    fun mapRawToLine(path:String){

    }

    fun csfToHIndex(path:String): List<HIndexLine> {
        val res = mutableListOf<HIndexLine>()
        csvReader() {
            delimiter = '\t'
            escapeChar = '\\'
            quoteChar = '"'
            charset = "ISO_8859_1"
        }.open(path) {
            readAllAsSequence().forEachIndexed { index, row ->
                if (index > 1) {
                    val csvLine = HIndexLine(
                        cleanTitle(row[2]),
                        row[5].replace(",", "").toInt(),
                        row[7].toInt()
                    )

                    res.add(csvLine)
                }
            }
        }

        return res.toList()
    }
}
