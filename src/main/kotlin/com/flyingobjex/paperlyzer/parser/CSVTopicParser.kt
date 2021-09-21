package com.flyingobjex.paperlyzer.parser

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.serialization.Serializable

enum class DisciplineType {
    STEM, SSH, M, NA;

    companion object {
        fun toType(value: String): DisciplineType {
            return when (value) {
                "STEM" -> STEM
                "STEM?" -> STEM
                "SSH" -> SSH
                "SSH?" -> SSH
                "M" -> M
                "M?" -> M
                else -> NA
            }
        }
    }
}

val omittedKeywords = listOf("science")

@Serializable
data class PLTopic(
    val name: String,
    val topicCluster: String,
    val topicFrequency: Int,
    val disciplineType: DisciplineType,
    val questionable: Boolean? = false,
    val keywords: List<String>,
    val leadKeyword: String? = null,
    val secondaryKeyword: String? = null,
) {
    companion object {
        fun blank() =
            PLTopic(
                "",
                "",
                0,
                DisciplineType.NA,
                false,
                emptyList(),
                ""
            )


    }
}

@Serializable
data class Discipline(
    val type: DisciplineType,
    val score: Int,
    val questionable: Boolean,
    val topics: List<String>? = null,
)

@Serializable
data class RawTopicLine(
    val topic: String,
    val topicCluster: String,
    val topicFrequency: String,
    val discipline: String,
)

fun isQuestionable(value: String) = value.contains("?")

fun toKeywords(value: String): List<String> =
    value.replace(",", "").replace(",", "").replace(",", "")
        .replace("&", "")
        .trim()
        .split(" ")
        .filter { it != "" }

@Serializable
data class MatchingCriteria(
    val exactMatch: Boolean,
    val oneKeyword: Boolean,
    val allKeywords: Boolean,
    val leadKeyword: Boolean,
    val secondaryKeyword: Boolean,
    val term: String,
    val topic: PLTopic,
    val index: Int,
) {
    val score: Int

    init {
        score = tallyScore()
    }

    private fun tallyScore(): Int {
        var total = 0
        if (exactMatch) {
            total += 54
        }

        if (oneKeyword) {
            total += 2
        }

        if (allKeywords) {
            total += 3
        }

        if (leadKeyword) {
            total += 16
        }

        if (secondaryKeyword) {
            total += 4
        }

        return total + (total * index)
    }

    fun hasOneTrue(): Boolean = exactMatch || oneKeyword || allKeywords || leadKeyword || secondaryKeyword
}



object CSVTopicParser {

    fun csvFileToTopicList(path: String): List<PLTopic> {
        val res = mutableListOf<PLTopic>()
        csvReader() {
            delimiter = '\t'
            escapeChar = '\\'
            quoteChar = '"'
            charset = "ISO_8859_1"
        }.open(path) {
            readAllAsSequence().forEachIndexed { index, row ->
                if (index > 1) {
                    val csvLine = RawTopicLine(
                        row[0],
                        row[1],
                        row[2],
                        row[3],
                    )
                    val topic = rawCsvTopicLineToTopic(csvLine)
                    res.add(topic)
                }
            }
        }
        val filtered = res.toList().filter { it.name != "" }

        return filtered
    }

    fun rawCsvTopicLineToTopic(line: RawTopicLine): PLTopic {
        val paperMetaData = PLTopic(
            line.topic,
            line.topicCluster,
            line.topicFrequency.toInt(),
            DisciplineType.toType(line.discipline),
            isQuestionable(line.discipline),
            toKeywords(line.topic),
            toLeadKeyword(line.topic),
            toSecondarKeyword(line.topic),
        )

        return paperMetaData
    }

    fun toSecondarKeyword(topic: String): String? {
        if (!topic.contains(",")) return null
        return topic.split(",").getOrNull(1)
    }

    fun toLeadKeyword(topic: String): String? {
        if (!topic.contains(",")) return null
        return topic.split(",").firstOrNull()
    }

}
