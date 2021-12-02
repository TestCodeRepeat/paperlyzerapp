package com.flyingobjex.paperlyzer.util

import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.IWosPaperWithAuthors
import com.flyingobjex.paperlyzer.repo.toShortKeys


object GenderUtils {

    fun allPapersAreGenderComplete(allAssociatedPapers: List<IWosPaperWithAuthors>): Boolean =
        allAssociatedPapers.filter { it.genderCompletenessScore == 1.0 }.size == allAssociatedPapers.size

    fun averageGenderRatio(allAssociatedPapers: List<IWosPaperWithAuthors>): Double? {
        val res = allAssociatedPapers
            .map {
                toGenderRatio(toShortKeys(it.authors), it.authors.size)
            }.filter {
                it >= 0
            }

        return if (res.size == 0) {
            null
        } else if (res.size == allAssociatedPapers.size) {
            res.sum() / res.size
        } else {
            null
        }
    }

    fun averageGenderRatioOfAuthors(allAuthors: List<Author>): Double? {
        return if (allAuthors.size > 1) {
            val res = toGenderRatio(toShortKeys(allAuthors), allAuthors.size)
            res / allAuthors.size
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
