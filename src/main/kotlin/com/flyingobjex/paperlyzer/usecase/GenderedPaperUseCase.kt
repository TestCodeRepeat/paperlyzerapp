package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentity
import com.flyingobjex.paperlyzer.entity.GenderedNameDetails
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.repo.matchGender
import com.flyingobjex.paperlyzer.repo.toShortKeys
import kotlin.system.measureTimeMillis
import org.litote.kmongo.`in`
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.setValue

class GenderedPaperUseCase(val mongo: Mongo) {

    private val wosRepo = WoSPaperRepository(mongo)

    fun resetGenderedPaperTable() {
        val resetTime = measureTimeMillis {
            mongo.genderedPapers.drop()

            mongo.rawPaperFullDetails.updateMany(
                WosPaper::processed ne false,
                listOf(
                    setValue(WosPaper::processed, false),
                )
            )

            mongo.resetIndexes()
        }

        println("GenderedPaperUseCase.kt :: resetGenderedPaperTable() :: resetTime = $resetTime")
    }

    fun printStats() {
        val totalGenderedPapers = mongo.genderedPapers.countDocuments()
        println("GenderedPaperUseCase.kt :: printStats() :: totalGenderedPapers = $totalGenderedPapers")
    }


    /** Paper Table */
    fun applyGendersToPaperTable(batchSize: Int) {

        val papers: List<WosPaper> = wosRepo.getPapersWithAuthors(batchSize)
        papers.parallelStream().forEach { paper ->

            val viableAuthors = paper.authors.filter { it.gender.gender == GenderIdentity.UNASSIGNED }

            val allGenderShortkeys = toShortKeys(paper.authors)
            val withoutFirstAuthor =
                if (allGenderShortkeys.length > 1)
                    allGenderShortkeys.subSequence(1, allGenderShortkeys.length - 1)
                else "-"

            if (viableAuthors.isNotEmpty()) {
                val matches = mongo.genderedNameDetails.find(
                    GenderedNameDetails::firstName `in` paper.authors.mapNotNull { it.firstName },
                ).toList()

                paper.authors.forEach { author ->
                    val match = matchGender(author.firstName, matches)
                    author.genderIdt = match?.genderIdentity
                    author.gender = Gender(match?.genderIdentity ?: GenderIdentity.NA, match?.probability ?: 0.0)
                }
                paper.authorGendersShortKey = allGenderShortkeys

                paper.firstAuthorGender = paper.authors.firstOrNull()?.gender?.gender?.toShortKey()
                paper.withoutFirstAuthorGender = withoutFirstAuthor.toString()

                val totalAuthors = paper.authors.size
                paper.totalAuthors = totalAuthors
                val identifiableAuthors = paper.authors
                    .filter {
                        it.gender.gender == GenderIdentity.MALE || it.gender.gender == GenderIdentity.FEMALE
                    }
                    .size
                paper.totalIdentifiableAuthors = identifiableAuthors
                val genderCompletenessScore = identifiableAuthors.toDouble() / totalAuthors.toDouble()
                paper.genderCompletenessScore = genderCompletenessScore

                if (genderCompletenessScore == 1.0) {
                    paper.genderCompletenessScore
                }

                mongo.genderedPapers.insertOne(paper)
                mongo.rawPaperFullDetails.updateOne(
                    WosPaper::_id eq paper._id,
                    setValue(WosPaper::processed, true)
                )

            } else {
                mongo.genderedPapers.insertOne(paper)
                mongo.rawPaperFullDetails.updateOne(
                    WosPaper::_id eq paper._id,
                    setValue(WosPaper::processed, true)
                )
            }
        }


    }
}
