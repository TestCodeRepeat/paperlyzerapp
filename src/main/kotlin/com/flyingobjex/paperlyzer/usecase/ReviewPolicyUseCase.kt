package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.ReviewPolicy
import com.flyingobjex.paperlyzer.entity.ReviewPolicyResponse
import com.flyingobjex.paperlyzer.entity.ReviewPolicyService
import com.flyingobjex.paperlyzer.entity.WosPaper
import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.litote.kmongo.*


data class WosPaperWithJournal(val _id: String?, val journal: String, val reviewPolicy: ReviewPolicy? = null)

data class ReivewPolicyStats(
    val mandatory: Long,
    val optional: Long,
    val notBlind: Long,
    val notPeerReviewed: Long,
    val blank: Long,
    val na: Long
)

class ReviewPolicyUseCase(val mongo: Mongo) {

    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    val policyAsText = File("./review-policy.json").readText()
    val policies = json.decodeFromString<ArrayList<ReviewPolicyResponse>>(policyAsText)

    val policyService = ReviewPolicyService(policies)

    fun printReviewPolicyStats(): ReivewPolicyStats {
        val mandatory = mongo.genderedPapers.countDocuments(
            WosPaper::policy eq ReviewPolicy.MandatoryBlind
        )

        val optional = mongo.genderedPapers.countDocuments(
            WosPaper::policy eq ReviewPolicy.OptionalBlind
        )

        val notBlind = mongo.genderedPapers.countDocuments(
            WosPaper::policy eq ReviewPolicy.NotBlind
        )

        val notPeerReviewed = mongo.genderedPapers.countDocuments(
            WosPaper::policy eq ReviewPolicy.NotPeerReviewed
        )

        val na = mongo.genderedPapers.countDocuments(
            WosPaper::policy eq ReviewPolicy.NA
        )

        val blank = mongo.genderedPapers.countDocuments(
            WosPaper::policy eq ReviewPolicy.Blank
        )

        return ReivewPolicyStats(
            mandatory = mandatory,
            optional = optional,
            notBlind = notBlind,
            notPeerReviewed = notPeerReviewed,
            blank = blank,
            na = na
        )

    }

    fun applyJournalPolicyToPapers() {
        var count = 0
        val res = mongo.genderedPapers.aggregate<WosPaperWithJournal>(
            match(WosPaper::_id ne null),
            project(
                WosPaper::_id from WosPaperWithJournal::_id,
                WosPaper::journal from WosPaperWithJournal::journal
            ),
        ).toList()

        res.parallelStream().forEach { paper ->
            val matchingPolicy = policyService.applyPolicy(paper.journal)
            if (matchingPolicy != ReviewPolicy.NA && matchingPolicy != ReviewPolicy.Blank) {
                count += 1
            }
            mongo.genderedPapers.updateOne(
                WosPaper::_id eq paper._id,
                listOf(setValue(WosPaper::policy, matchingPolicy))
            )
        }

        println("ReviewPolicyUseCase.kt :: found ${count} matching journals")

    }

    fun updateJournalTitlesToUppercase() {
        var count = 0
        val res = mongo.genderedPapers.aggregate<WosPaperWithJournal>(
            match(WosPaper::_id ne null),
            project(
                WosPaper::_id from WosPaperWithJournal::_id,
                WosPaper::journal from WosPaperWithJournal::journal
            ),
        ).toList()

        res.parallelStream().forEach { paper ->
            if (containsLowerCaseChar(paper.journal)) {
                println("ReviewPolicyUseCase.kt :: has lowercase :: ${paper.journal}")
                count += 1
                val processed = trimString(paper.journal)
                println("ReviewPolicyUseCase.kt :: updateJournalTitlesToUppercase() :: processed = ${processed}")
                mongo.genderedPapers.updateOne(
                    WosPaper::_id eq paper._id,
                    setValue(WosPaper::journal, processed)
                )
            }
        }

        println("ReviewPolicyUseCase.kt :: updateJournalTitlesToUppercase() :: count = ${count}")
    }

    private fun trimString(str: String): String {
        return if (str.toCharArray().last().toString() == "," && str.length > 2) {
            println("ReviewPolicyUseCase.kt :: trimString() :: str = $str")
            val res = str.slice(IntRange(0, str.length - 2)).uppercase()
            res
        } else {
            str.uppercase()
        }
    }

    private fun containsLowerCaseChar(title: String): Boolean {
        val asChars = title.toCharArray()
        val (lower, upper) = asChars.partition { it.isLowerCase() }
        return lower.isNotEmpty()
    }
}
