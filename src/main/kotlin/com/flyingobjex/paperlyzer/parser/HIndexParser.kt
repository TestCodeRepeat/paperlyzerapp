package com.flyingobjex.paperlyzer.parser

import com.flyingobjex.paperlyzer.util.JsonUtils
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString


data class HIndexLine(val title: String, val sjr: Int, val hIndex: Int)

fun cleanTitle(title: String): String {
    return title
        .lowercase()
        .replace("&amp;", "")
        .replace("&amp;", "&")
        .replace(", the", "")
        .trim()
}

data class SJRScore(val sjrRank: SJRank, val score: Int)

val re = Regex("[^A-Za-z0-9 ]")
fun clean(value:String):String =
    re.replace(value, " ")
        .replace("  ", " ")
        .uppercase()
        .trim()

class SJRModel() {

    private val sjrRankings: List<SJRank>

    init {
        val contents = JsonUtils.loadResourceFile("sjrscore.json")
        sjrRankings = JsonUtils.json.decodeFromString<ArrayList<SJRank>>(contents)
            .map { it.copy(title = clean(it.title)) }
    }



    fun matchJournalTitleToSJRank(title: String): SJRank? {
        val t = clean(title)
        val res = sjrRankings.filter { it.title == t }
        return res.firstOrNull()
    }

}


@Serializable
data class SJRank(
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

    fun csfToHIndex(path: String): List<HIndexLine> {
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
