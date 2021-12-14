package com.flyingobjex.paperlyzer.util

object StringUtils {

    fun aliasesToName(aliases: List<String>): String? = aliases.maxByOrNull { it.length }
}
