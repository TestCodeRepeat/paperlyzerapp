package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.WosPaper
import org.litote.kmongo.*

class ReviewPolicyUseCase(val mongo: Mongo) {

    fun updateJournalTitlesToUppercase() {
        var count = 0
        mongo.genderedPapers.aggregate<WosPaper>(
            match(WosPaper::ssAuthorProcessedStep1 ne true),
            limit(150000)
        ).toList()
            .parallelStream().forEach { title ->
                if (containsLowerCaseChar(title.journal)) {
                    println("ReviewPolicyUseCase.kt :: has lowercase :: ${title.journal}")
                    count += 1
//                    mongo.genderedPapers.updateOne(
//                        WosPaper::_id eq title._id,
//                        setValue(WosPaper::journal, title.journal.uppercase())
//                    )
                }
            }

        println("ReviewPolicyUseCase.kt :: updateJournalTitlesToUppercase() :: count = ${count}")
    }

    private fun containsLowerCaseChar(title: String): Boolean {
        val asChars = title.toCharArray()
        val (lower, upper) = asChars.partition { it.isLowerCase() }
        return lower.isNotEmpty()
    }
}
