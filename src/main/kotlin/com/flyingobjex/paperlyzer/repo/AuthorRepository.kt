package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.GenderedAuthorTableStats
import com.flyingobjex.paperlyzer.entity.*
import com.flyingobjex.paperlyzer.parser.DisciplineType
import com.flyingobjex.paperlyzer.process.StemSshAuthorProcessStats
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.*
import java.util.logging.Logger

data class AuthorTableStats(
    val rawAuthorsUpdated: Long,
    val rawAuthorsPending: Long,
    val totalAuthors: Long,
    val initialsOnly: Long,
    val noFirstName: Long,
    val na: Long,
    val assignableNames: Long,
){
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

    /** Author STEM / SSH -  */

    fun getUnprocessedAuthorsByAuthorReport(batchSize: Int): List<Author> =
        mongo.genderedAuthors.aggregate<Author>(
            match(Author::authorReportUnprocessed eq true),
            limit(batchSize)
        ).toList()

    fun unprocessedAuthorsByAuthorReportCount() =
        mongo.genderedAuthors.countDocuments(Author::authorReportUnprocessed eq true)

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
            initialsOnly = mongo.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.INITIALS),
            noFirstName = mongo.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.NOFIRSTNAME),
            na = mongo.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.NA),
            assignableNames = mongo.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED)
        )
    }

    /** First Name Table */
    fun buildFirstNameTableFromSsAuthorTable(batchSize: Int) {
        val unprocessedBatch: List<Author> = mongo.authors.find(
                SemanticScholarAuthor::firstNameProcessed ne true
        ).limit(batchSize).toList()

        unprocessedBatch.parallelStream().forEach { ssAuthor ->
            // TODO - complete implementation
        }
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

    /** General Accessors */
    fun insertManyAuthors(authors: List<Author>) {
        mongo.rawAuthors.insertMany(authors)
    }

    fun updateAuthorUnprocessedForAuthorReport(author: Author): UpdateResult =
        mongo.genderedAuthors.updateOne(
            Author::_id eq author._id,
            setValue(Author::authorReportUnprocessed, false)
        )

    fun resetAuthorReport(): UpdateResult =
        mongo.genderedAuthors.updateMany(
            Author::authorReportUnprocessed ne true,
            setValue(Author::authorReportUnprocessed, true)
        )

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


