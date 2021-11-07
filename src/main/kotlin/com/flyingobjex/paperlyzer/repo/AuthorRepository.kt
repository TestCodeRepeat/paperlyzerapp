package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.AuthorTableStats
import com.flyingobjex.paperlyzer.entity.*
import com.flyingobjex.paperlyzer.process.CoAuthorProcessStats
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.*
import java.util.logging.Logger

class AuthorRepository(val mongo: Mongo) {
    val log: Logger = Logger.getAnonymousLogger()

    fun resetCoAuthorData() {
        mongo.genderedAuthors.updateMany(
            Author::averageCoAuthors ne -5.0,
            listOf(
                setValue(Author::averageCoAuthors, -5.0),
                setValue(Author::totalPapers, -5)
            )
        )
    }

    fun getSsUnprocessedAuthors(batchSize: Int): List<Author> {
        return mongo.genderedAuthors.find(Author::ssProcessedYearsPub eq false)
            .limit(batchSize).toList()
    }

    fun buildAuthorTableInParallel(batchSize: Int) {
        val batch = mongo.rawAuthors.find(
            and(
                Author::duplicateCheck eq false,
                Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED
            )
        ).limit(batchSize).toList()

        log.info("AuthorTableRepo.buildAuthorTableInParallel()  batch.size = " + batch.size)
        batch.parallelStream().forEach { targetAuthor ->

            // Find all matching first & last name records
            val matchingOnNames = mongo.authors.find(
                and(
                    Author::lastName eq targetAuthor.lastName,
                    Author::firstName eq targetAuthor.firstName,
                )
            ).asSequence()

            if (targetAuthor.orcID == null) {
                val matched = matchingOnNames.firstOrNull()

                if (matched != null) {
                    matched.papers?.addAll(targetAuthor?.papers ?: emptyList())
                    matched.paperCount = matched.papers?.size ?: 0
                    matched.applyFirstLastYearsPublished()
                    mongo.authors.updateOne(matched)

                } else {
                    targetAuthor.applyFirstLastYearsPublished()
                    mongo.authors.insertOne(targetAuthor)
                }

            } else {
                // find matching orcId from results
                val matched = matchingOnNames.firstOrNull { it.orcIDString == targetAuthor.orcIDString }
                if (matched != null) {
                    // update record in database & bail out
                    matched.papers?.addAll(targetAuthor?.papers ?: emptyList())
                    matched.paperCount = matched.papers?.size ?: 0
                    targetAuthor.applyFirstLastYearsPublished()
                    mongo.authors.updateOne(matched)

                } else {
                    // no matching orcId, find record with null orcId to add paper to
                    val authorWithNoOrcId = matchingOnNames.firstOrNull { it.orcIDString == null }
                    if (authorWithNoOrcId != null) {
                        authorWithNoOrcId.papers?.addAll(targetAuthor?.papers ?: emptyList())
                        authorWithNoOrcId.paperCount = authorWithNoOrcId.papers?.size ?: 0
                        authorWithNoOrcId.applyFirstLastYearsPublished()
                        mongo.authors.updateOne(authorWithNoOrcId)

                    } else {  // all records alread have an orcId, create a new record
                        targetAuthor.applyFirstLastYearsPublished()
                        mongo.authors.insertOne(targetAuthor)
                    }
                }
                // if no matching orcId, take first result with no orcId
            }

            mongo.rawAuthors.updateOne(
                Author::_id eq targetAuthor._id,
                setValue(Author::duplicateCheck, true)
            )
        }
    }

    fun buildGenderedAuthorsTable(batchSize: Int) {
        val batch: List<Author> = mongo.authors.find(
            and(
                Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED,
            )
        ).limit(batchSize).toList()

        batch.parallelStream().forEach { targetAuthor ->
            mongo.genderTable.findOne(
                GenderDetails::firstName eq targetAuthor.firstName
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

    fun statsGenderedAuthorsTable(): AuthorTableStats {
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

        return AuthorTableStats(
            totalAuthors = totalAuthors,
            totalFemaleAuthors = totalFemaleNames,
            totalMaleAuthors = totalMaleNames,
            totalWithNoAssignedGender = noAssignment
        )
    }

    fun insertManyAuthors(authors: List<Author>) {
        mongo.rawAuthors.insertMany(authors)
    }

    fun buildFirstNameTable(batchSize: Int) {
        val batch: List<Author> = mongo.authors.find(
            and(
                Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED,
            )
        ).limit(batchSize).toList()

        log.info("AuthorTableRepo.buildFirstNameTable()  batch.size = ${batch.size}")
        batch.parallelStream().forEach { targetAuthor ->

            if (targetAuthor?.firstName != null && !isAbbreviation(targetAuthor.firstName)) {
                val potentialDuplicate = mongo.firstNameTable
                    .findOne(FirstName::firstName eq targetAuthor.firstName)

                if (potentialDuplicate == null) {
                    mongo.firstNameTable.insertOne(
                        FirstName(
                            targetAuthor.firstName,
                            targetAuthor.firstName,
                            targetAuthor.lastName
                        )
                    )
                }
            }

        }
    }

    fun getCoAuthorStats(): CoAuthorProcessStats {
        val totalAuthors = mongo.genderedAuthors.countDocuments()
        val totalProcessed = mongo.genderedAuthors.countDocuments(Author::averageCoAuthors gt -5.0)
        return CoAuthorProcessStats(
            totalProcessedWithCoAuthorData = totalProcessed,
            totalUnprocessed = unprocessedAuthorsCount(),
            totalAuthors = totalAuthors,
            0
        )
    }

    fun unprocessedAuthorsCount(): Long {
        return mongo.genderedAuthors.countDocuments(
            or(
                Author::averageCoAuthors eq -5.0,
                Author::averageCoAuthors eq null
            )
        )
    }

    fun getUnprocessedAuthorsByCoAuthors(batchSize: Int): List<Author> {
        return mongo.genderedAuthors.find(
            or(
                Author::averageCoAuthors eq -5.0,
                Author::averageCoAuthors eq null,
            ),
            limit(batchSize)
        ).toList()
    }

    fun updateAuthor(author: Author): UpdateResult =
        mongo.genderedAuthors.updateOne(
            Author::_id eq author._id,
            listOf(
                setValue(Author::totalPapers, author.totalPapers),
                setValue(Author::averageCoAuthors, author.averageCoAuthors),
            )
        )
}

fun isAbbreviation(value: String): Boolean {
    return (value.length <= 2 ||
        (value.contains(".") && value.length <= 2) ||
        (hasTwoDots(value) && value.length < 6) ||
        isAllCaps(value)
        )
}

fun Author.hasNullPublishedYear(): Boolean {
    return (firstYearPublished != null && firstYearPublished ?: 0 == 0) || (lastYearPublished != null && lastYearPublished ?: 0 == 0)
}

fun Author.applyFirstLastYearsPublished(): Pair<Int, Int> {
    val sorted = toYearsPublishedAsInt().sorted()
    val first = sorted.firstOrNull() ?: 0
    val last = sorted.lastOrNull() ?: 0
    this.firstYearPublished = first
    this.lastYearPublished = last
    return Pair(first, last)
}
