package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.*
import java.util.logging.Logger
import org.litote.kmongo.*

data class AuthorTableStats(
    val rawAuthorsUpdated: Long,
    val rawAuthorsPending: Long,
    val totalAuthors: Long,
    val initialsOnly: Long,
    val noFirstName: Long,
    val na: Long,
    val assignableNames: Long,
) {
    override fun toString(): String = "" +
        "================================================================\n" +
        "================================================================\n" +
        "   :::: Author Table Stats :::: \n" +
        "rawAuthorsUpdated = $rawAuthorsUpdated \n" +
        "rawAuthorsPending = $rawAuthorsPending \n" +
        "totalAuthors = $totalAuthors \n" +
        "initialsOnly = $initialsOnly \n" +
        "noFirstName = $noFirstName \n" +
        "na = $na \n" +
        "assignableNames = $assignableNames \n" +
        "Authors Table combined total = ${assignableNames + na + noFirstName + initialsOnly} \n" +
        "================================================================\n " +
        "================================================================\n "
}

class AuthorRepository(val mongo: Mongo) {
    val log: Logger = Logger.getAnonymousLogger()

    /** Semantic Scholar  */
    fun getSsUnprocessedAuthors(batchSize: Int): List<Author> {
        return mongo.genderedAuthors.find(Author::ssProcessedYearsPub eq false)
            .limit(batchSize).toList()
    }

    /** Author Table */
    fun buildAuthorTableInParallel(batchSize: Int) {
        val batch = mongo.rawAuthors.find(
            and(
                Author::duplicateCheck eq false,
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

    /** Author Table Stats */
    fun getAuthorTableStats(): AuthorTableStats {

        return AuthorTableStats(
            rawAuthorsPending = mongo.rawAuthors.countDocuments(Author::duplicateCheck eq false),
            rawAuthorsUpdated = mongo.rawAuthors.countDocuments(Author::duplicateCheck eq true),
            totalAuthors = mongo.authors.countDocuments(),
            initialsOnly = mongo.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentity.INITIALS),
            noFirstName = mongo.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentity.NOFIRSTNAME),
            na = mongo.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentity.NA),
            assignableNames = mongo.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentity.UNASSIGNED)
        )
    }

    /** General Accessors */
    fun insertManyAuthors(authors: List<Author>) {
        mongo.rawAuthors.insertMany(authors)
    }

    fun updateGenderedAuthor(author: Author) = mongo.genderedAuthors.updateOne(author)

    fun resetRawAuthors() {
        mongo.rawAuthors.updateMany(
            Author::duplicateCheck ne false,
            setValue(Author::duplicateCheck, false)
        )
    }

    fun clearRawData() {
        log.info("AuthorRepository.clearRawData()  DATA CLEARING !!!  dropping Raw Author & Raw Paper Details")
        mongo.rawAuthors.drop()
        mongo.rawPaperFullDetails.drop()
    }

}

fun isAbbreviation(value: String): Boolean {
    if (value.length == 2 &&
        value.getOrNull(0)?.isUpperCase() == true &&
        value.getOrNull(1)?.isLowerCase() == true
    ) {
        return false
    }
    return (value.length <= 2 ||
        (value.contains(".") && value.length <= 2) ||
        (hasTwoDots(value) && value.length < 6) ||
        isAllCaps(value)
        )
}


