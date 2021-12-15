package com.flyingobjex.paperlyzer.util

import java.util.logging.Logger


data class Name(val firstName: String?, val middleNames: String?, val lastName: String?)

object StringUtils {
    val log: Logger = Logger.getAnonymousLogger()

    fun aliasesToName(aliases: List<String>): String = aliases.maxByOrNull { it.length } ?: ""

    fun aliasToLongestMiddleName(aliases: List<String>): String? =
        aliases
            .map {
                it.split(" ")
            }
            .map {
                if (it.size > 2) {
                    return@map it.subList(1, it.size - 1).joinToString(" ")
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
