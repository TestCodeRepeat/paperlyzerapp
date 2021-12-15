package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentitiy
import com.flyingobjex.paperlyzer.entity.SemanticScholarAuthor
import com.flyingobjex.paperlyzer.repo.FirstName
import com.flyingobjex.paperlyzer.repo.isAbbreviation
import com.flyingobjex.paperlyzer.util.StringUtils.aliasToLongestLastName
import com.flyingobjex.paperlyzer.util.StringUtils.aliasToLongestMiddleName
import com.flyingobjex.paperlyzer.util.StringUtils.aliasesToLongestFirstName
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

            targetAuthor.aliases?.let { aliases ->
                val firstName = aliasesToLongestFirstName(aliases)
                val middleName = aliasToLongestMiddleName(aliases)
                val lastName = aliasToLongestLastName(aliases)

                mongo.ssAuthors.updateOne(
                    SemanticScholarAuthor::_id eq targetAuthor._id,
                    listOf(
                        setValue(SemanticScholarAuthor::firstName, firstName),
                        setValue(SemanticScholarAuthor::middleName, middleName),
                        setValue(SemanticScholarAuthor::lastName, lastName),
                    )
                )
            }

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
