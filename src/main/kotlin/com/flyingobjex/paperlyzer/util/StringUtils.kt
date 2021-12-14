package com.flyingobjex.paperlyzer.util


data class Name(val firstName: String?, val middleName: String?, val lastName: String?)

object StringUtils {

    fun splitToFirstLastNames(name: String): Name {
        val split = name.split(" ")
        val first = split.firstOrNull()
        val last = split.lastOrNull()
        val m = split.getOrNull(1)
        val middle = if (m == last) null else m
        return Name(first, middle, last)

    }

    fun aliasesToName(aliases: List<String>): String = aliases.maxByOrNull { it.length } ?: ""
}
