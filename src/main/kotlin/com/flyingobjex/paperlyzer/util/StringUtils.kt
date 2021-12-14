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

    fun aliasToLongestMiddleName(aliases: List<String>): String? =
        aliases
            .map {
                it.split(" ")
            }
            .map {
                if (it.size > 2) {
                    val start = 1
                    val end = it.size - 1
                    val middles = it.subList(start, end)
                    return@map middles.joinToString(" ")
                } else {
                    null
                }
            }
            .maxByOrNull { it?.length ?: 0 }


    fun aliasToLongestLastName(aliases: List<String>): String? =
        aliases.map {
            it.split(" ")
        }.maxByOrNull {
            it.lastOrNull()?.length ?: 0
        }?.lastOrNull()


    fun aliasesToLongestFirstName(aliases: List<String>): String? =
        aliases
            .map {
                it.split(" ")
            }.maxByOrNull {
                it.firstOrNull()?.length ?: 0
            }?.firstOrNull()

}
