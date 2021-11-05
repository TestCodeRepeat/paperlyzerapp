package com.flyingobjex.paperlyzer.parser

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader


data class HIndexLine(val title:String, val sjr: Int, val hIndex:Int )

fun cleanTitle(title:String):String{
    return title
        .lowercase()
        .replace("&amp;", "")
        .replace("&amp;", "&")
        .replace(", the", "")
        .trim()
}

object CSVHIndexParser {

    fun csfToHIndex(path:String){
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

//                    val topic = CSVTopicParser.rawCsvTopicLineToTopic(csvLine)
//                    res.add(topic)
                }
            }
        }
//        val filtered = res.toList().filter { it.name != "" }
//        return filtered
    }
}
