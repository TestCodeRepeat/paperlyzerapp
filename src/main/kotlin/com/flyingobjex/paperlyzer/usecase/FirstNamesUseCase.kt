package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentitiy
import com.flyingobjex.paperlyzer.entity.SemanticScholarAuthor
import com.flyingobjex.paperlyzer.repo.FirstName
import com.flyingobjex.paperlyzer.repo.isAbbreviation
import com.flyingobjex.paperlyzer.util.StringUtils
import com.flyingobjex.paperlyzer.util.StringUtils.aliasesToName
import com.flyingobjex.paperlyzer.util.StringUtils.splitToFirstLastNames
import java.util.logging.Logger
import org.litote.kmongo.*

const val UNINITIALIZED = "uninitialized"

class FirstNamesUseCase(val mongo: Mongo) {

    val log: Logger = Logger.getAnonymousLogger()


    /** Apply calculated names to Semantic Scholar Authors */
    fun mapAliasToFullNamesOnSsAuthorTable(batchSize: Int) {
        val batch: List<SemanticScholarAuthor> = mongo.ssAuthors.find(
            SemanticScholarAuthor::firstName eq UNINITIALIZED
        ).limit(batchSize).toList()

        batch.parallelStream().forEach { targetAuthor ->

            val name = splitToFirstLastNames(aliasesToName(targetAuthor.aliases ?: emptyList()))
            mongo.ssAuthors.updateOne(
                SemanticScholarAuthor::_id eq targetAuthor._id,
                listOf(
                    setValue(SemanticScholarAuthor::firstName, name.firstName),
                    setValue(SemanticScholarAuthor::middleName, name.middleName),
                    setValue(SemanticScholarAuthor::lastName, name.lastName),
                )
            )

        }
    }

    fun resetMapAliasToNames() {
        mongo.ssAuthors.updateMany(
            SemanticScholarAuthor::firstName ne UNINITIALIZED,
            listOf(
                setValue(SemanticScholarAuthor::firstName, UNINITIALIZED),
                setValue(SemanticScholarAuthor::lastName, UNINITIALIZED),
                setValue(SemanticScholarAuthor::middleName, UNINITIALIZED),
            )
        )
    }

    private fun addToFirstNamesTable(batch: List<Author>) {
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

    /** First Name Table */
    fun buildFirstNameTableFromSsAuthorTable(batchSize: Int) {
        val batch: List<Author> = mongo.authors.find(
            SemanticScholarAuthor::firstNameProcessed ne true
        ).limit(batchSize).toList()

        addToFirstNamesTable(batch)
    }

    fun buildFirstNameTableFromWosAuthors(batchSize: Int) {
        val batch: List<Author> = mongo.authors.find(
            and(
                Author::gender / Gender::gender eq GenderIdentitiy.UNASSIGNED,
            )
        ).limit(batchSize).toList()

        addToFirstNamesTable(batch)
    }

}
