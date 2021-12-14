package com.flyingobjex.paperlyzer.util

import java.util.logging.Logger


data class Name(val firstName: String?, val middleName: String?, val lastName: String?)

object StringUtils {
    val log: Logger = Logger.getAnonymousLogger()

    fun splitToFirstLastNames(name: String): Name {
        val split = name.split(" ")
        val first = split.firstOrNull()
        val last = split.lastOrNull()
        val m = split.getOrNull(1)
        val middle = if (m == last) null else m
        return Name(first, middle, last)

    }

    fun aliasesToName(aliases: List<String>): String = aliases.maxByOrNull { it.length } ?: ""

    fun aliasesToLongestFirstName(aliases: List<String>): String? {
        val res = aliases
            .map {
                it.split(" ")
            }
            .sortedByDescending {
                it.getOrNull(0)?.length
            }

        log.info("StringUtils.aliasesToLongestFirstName()  res = ${res}" )
        return res.firstOrNull()?.firstOrNull()
    }
}
