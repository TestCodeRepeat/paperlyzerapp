package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.GenderedAuthorTableStats
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentitiy
import com.flyingobjex.paperlyzer.entity.GenderedNameDetails
import org.litote.kmongo.*

class GenderedAuthorUseCase(val mongo: Mongo) {

    fun getGenderedAuthors(querySize: Int): List<Author> =
        mongo.genderedAuthors.find(
            or(
                Author::gender / Gender::gender eq GenderIdentitiy.MALE,
                Author::gender / Gender::gender eq GenderIdentitiy.FEMALE,
            )
        ).limit(querySize).toList()

    fun buildGenderedAuthorsTable(batchSize: Int) {
        val batch: List<Author> = mongo.authors.find(
            and(
                Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED,
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
                Author::genderIdt eq GenderIdentitiy.UNASSIGNED,
                Author::genderIdt eq GenderIdentitiy.NOFIRSTNAME,
                Author::genderIdt eq GenderIdentitiy.INITIALS,
            )
        ).count()
        val totalFemaleNames = mongo.genderedAuthors.find(
            or(
                Author::genderIdt eq GenderIdentitiy.FEMALE
            )
        ).count()
        val totalMaleNames = mongo.genderedAuthors.find(
            or(
                Author::genderIdt eq GenderIdentitiy.MALE
            )
        ).count()

        return GenderedAuthorTableStats(
            totalAuthors = totalAuthors,
            totalFemaleAuthors = totalFemaleNames,
            totalMaleAuthors = totalMaleNames,
            totalWithNoAssignedGender = noAssignment
        )
    }

}
