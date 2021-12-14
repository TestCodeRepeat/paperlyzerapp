package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentitiy
import com.flyingobjex.paperlyzer.entity.SemanticScholarAuthor
import com.flyingobjex.paperlyzer.repo.FirstName
import com.flyingobjex.paperlyzer.repo.isAbbreviation
import java.util.logging.Logger
import org.litote.kmongo.*

class FirstNamesUseCase(val mongo: Mongo) {

    val log: Logger = Logger.getAnonymousLogger()

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

}
