package com.flyingobjex.paperlyzer.util

import com.flyingobjex.paperlyzer.repo.WosPaperWithAuthors
import com.flyingobjex.paperlyzer.repo.toShortKeys

object GenderUtils {

    fun averageGenderRatio(allAssociatedPapers: List<WosPaperWithAuthors>): Double? {
        val res = allAssociatedPapers
            .map {
                toGenderRatio(toShortKeys(it.authors), it.authors.size)
            }.filter {
                it >= 0
            }

        return if (res.size == allAssociatedPapers.size){
            res.sum() / res.size
        } else {
            null
        }
    }

    fun toGenderRatio(shortKeys: String, numAuthors: Int): Double {
        val res = shortKeys.sumOf {
            val res = when (it.toString()) {
                "M" -> 1
                "F" -> 0
                else -> -99999
            }
            res
        }.toDouble() / numAuthors.toDouble()

        return if (res >= 0) res else -5.0
    }

}
