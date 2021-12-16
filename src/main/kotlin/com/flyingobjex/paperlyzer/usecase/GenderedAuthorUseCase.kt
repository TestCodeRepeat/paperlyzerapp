package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.GenderedAuthorTableStats
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentity
import com.flyingobjex.paperlyzer.entity.GenderedNameDetails
import java.util.*
import org.litote.kmongo.*

class GenderedAuthorUseCase(val mongo: Mongo) {

    fun getGenderedAuthors(querySize: Int): List<Author> =
        mongo.genderedAuthors.find(
            or(
                Author::gender / Gender::gender eq GenderIdentity.MALE,
                Author::gender / Gender::gender eq GenderIdentity.FEMALE,
            )
        ).limit(querySize).toList()

    fun buildGenderedAuthorsTable(batchSize: Int) {
        val batch: List<Author> = mongo.authors.find(
            and(
                Author::gender / Gender::gender eq GenderIdentity.UNASSIGNED,
            )
        ).limit(batchSize).toList()

        batch.parallelStream().forEach { targetAuthor ->
            mongo.genderedNameDetails.findOne(
                GenderedNameDetails::firstName eq targetAuthor.firstName
            )
                ?.let { matchingGender ->
                    targetAuthor.gender.gender = matchingGender.genderIdentity
                    targetAuthor.gender.probability = matchingGender.probability
                    targetAuthor.genderIdt = matchingGender.genderIdentity
                    targetAuthor.probabilityStr = matchingGender.probability
                    mongo.genderedAuthors.insertOne(targetAuthor)
                }
        }
    }

    fun statsGenderedAuthorsTable(): GenderedAuthorTableStats {
        val totalAuthors = mongo.genderedAuthors.countDocuments()
        val noAssignment = mongo.genderedAuthors.find(
            or(
                Author::genderIdt eq GenderIdentity.UNASSIGNED,
                Author::genderIdt eq GenderIdentity.NOFIRSTNAME,
                Author::genderIdt eq GenderIdentity.INITIALS,
            )
        ).count()
        val totalFemaleNames = mongo.genderedAuthors.find(
            or(
                Author::genderIdt eq GenderIdentity.FEMALE
            )
        ).count()
        val totalMaleNames = mongo.genderedAuthors.find(
            or(
                Author::genderIdt eq GenderIdentity.MALE
            )
        ).count()

        return GenderedAuthorTableStats(
            totalAuthors = totalAuthors,
            totalFemaleAuthors = totalFemaleNames,
            totalMaleAuthors = totalMaleNames,
            totalWithNoAssignedGender = noAssignment
        )
    }

    fun resetBuildGenderedAuthors() {
        mongo.clearGenderedAuthorsTable()
        println("GenderedAuthorUseCase.kt :: resetBuildGenderedAuthors :: clearGenderedAuthorsTable()")
        mongo.resetIndexes()
        println("GenderedAuthorUseCase.kt :: resetBuildGenderedAuthors :: mongo.resetIndexes()")
    }

}
