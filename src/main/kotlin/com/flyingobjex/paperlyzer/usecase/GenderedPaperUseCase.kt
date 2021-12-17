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
import org.litote.kmongo.*

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

        // Get unprocessed raw
        val unprocessed = mongo.rawPaperFullDetails.aggregate<WosPaper>(
            match(
                or(
                    WosPaper::processed eq false,
                    WosPaper::processed eq null
                )
            )
        ).take(batchSize).toList()

        val papers = unprocessed
//        val papers: List<WosPaper> = wosRepo.getPapersWithAuthors(batchSize)

        papers.parallelStream().forEach { rawPaper ->

            val viableAuthors = rawPaper.authors.filter { it.gender.gender == GenderIdentity.UNASSIGNED }

            if (viableAuthors.isNotEmpty()) {

                val matches = mongo.genderedNameDetails.find(
                    GenderedNameDetails::firstName `in` rawPaper.authors.mapNotNull { it.firstName },
                ).toList()

                val genderedAuthors = rawPaper.authors.map { author ->
                    val match = matchGender(author.firstName, matches)
                    val gender = Gender(match?.genderIdentity ?: GenderIdentity.NA, match?.probability ?: 0.0)
                    author.copy(gender = gender, genderIdt = gender.gender)
                }

                val allGenderShortkeys = toShortKeys(genderedAuthors)

                val withoutFirstAuthorGender =
                    if (allGenderShortkeys.length > 1)
                        allGenderShortkeys.subSequence(1, allGenderShortkeys.length).toString()
                    else "-"

                val firstAuthorGender = genderedAuthors.firstOrNull()?.gender?.gender?.toShortKey()
                val totalAuthors = genderedAuthors.size

                val identifiableAuthors = genderedAuthors
                    .filter {
                        it.gender.gender == GenderIdentity.MALE || it.gender.gender == GenderIdentity.FEMALE
                    }
                    .size

                val genderCompletenessScore = identifiableAuthors.toDouble() / totalAuthors.toDouble()

                if (genderCompletenessScore == 1.0) {
                    rawPaper.genderCompletenessScore
                }

                val updatedPaper = rawPaper.copy(
                    authors = genderedAuthors,
                    authorGendersShortKey = allGenderShortkeys,
                    firstAuthorGender = firstAuthorGender,
                    withoutFirstAuthorGender = withoutFirstAuthorGender,
                    totalAuthors = totalAuthors,
                    totalIdentifiableAuthors = identifiableAuthors,
                    genderCompletenessScore = genderCompletenessScore
                )

//                paper.authorGendersShortKey = allGenderShortkeys
//                paper.firstAuthorGender = paper.authors.firstOrNull()?.gender?.gender?.toShortKey()
//                paper.withoutFirstAuthorGender = withoutFirstAuthor.toString()
//                paper.totalAuthors = totalAuthors
//                paper.totalIdentifiableAuthors = identifiableAuthors
//                paper.genderCompletenessScore = genderCompletenessScore


                mongo.genderedPapers.insertOne(updatedPaper)
                mongo.rawPaperFullDetails.updateOne(
                    WosPaper::_id eq rawPaper._id,
                    setValue(WosPaper::processed, true)
                )

            } else {
                mongo.genderedPapers.insertOne(rawPaper)
                mongo.rawPaperFullDetails.updateOne(
                    WosPaper::_id eq rawPaper._id,
                    setValue(WosPaper::processed, true)
                )
            }
        }


    }
}
