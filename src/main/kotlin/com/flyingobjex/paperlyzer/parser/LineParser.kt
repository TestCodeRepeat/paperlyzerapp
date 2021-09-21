package com.flyingobjex.paperlyzer.parser

import com.flyingobjex.paperlyzer.entity.LineItem

object LineParser {

    val regexHasAllCaps = "[A-Z<\n]+".toRegex()
    private const val keyEndingCharPosition = 3

    fun cleanOrcId(value:String):String = value.replace(";", "")


    fun stringToLines(value: String): List<LineItem> {
        val splitByReturn = value.split("\n")
        val lines = splitByReturn
            .asSequence()
            .map { it.trim() }
            .map { rawLine ->
                if (hasKey(rawLine)) {
                    LineItem(toKey(rawLine), toBody(rawLine))
                } else {
                    LineItem(null, rawLine)
                }
            }

        return lines.toList()
    }

    internal fun toKey(line: String): String = line.substring(0, 2)

    internal fun toBody(line: String): String {
        if (line.length >= keyEndingCharPosition) {
            return line.substring(keyEndingCharPosition, line.length)
        } else {
            return line
        }
    }

    internal fun hasKey(line: String): Boolean {
        if (line.length == 2 && regexHasAllCaps.matches(line)) return true
        if (line.length < keyEndingCharPosition) return false
        val firstTwoChars = line.substring(0, 2)
        val firstThreeChars =
            if (line.length >= keyEndingCharPosition) line.substring(0, keyEndingCharPosition) else line.substring(0, line.length)
        return regexHasAllCaps.matches(firstTwoChars) && firstThreeChars.contains(" ")
    }

}