package com.flyingobjex.paperlyzer.util

import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.IWosPaperWithAuthors
import com.flyingobjex.paperlyzer.util.CollectionUtils.Companion.withoutFirst
import com.flyingobjex.paperlyzer.util.CollectionUtils.Companion.withoutLast


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
        if (numAuthors != shortKeys.length) {
            throw Exception("Gender Ratio Error -- Source Keys . Size does not match Number of Authors !!!\n" +
                "shortKeys.length = ${shortKeys.length} :: numAuthors = $numAuthors")
        }
        val res = shortKeys.sumOf {
            val res = when (it.toString()) {
                "M" -> 1
                "F" -> 0
                else -> -99999
            }
            res
        }.toDouble() / numAuthors.toDouble()

        return if (res >= 0) res else -55.5555555555
    }

//    fun genderRatioWithoutFirst(authors: List<Author>) =
//        toGenderRatio(toShortKeys(withoutFirst(authors)), authors.size - 1)

//    fun genderRatioWithoutLast(authors: List<Author>) =
//        toGenderRatio(toShortKeys(withoutLast(authors)), authors.size - 1)

    fun toShortKeys(authors: List<Author>): String =
        authors.joinToString("") { it.genderIdt?.toShortKey() ?: "X" }

}
