package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.*

class AuthorReportUseCase(val mongo: Mongo) {

    /** Report */
    fun getUnprocessedAuthorsByAuthorReport(batchSize: Int): List<Author> =
        mongo.genderedAuthors.aggregate<Author>(
            match(Author::authorReportUnprocessed eq true),
            limit(batchSize)
        ).toList()

    fun unprocessedAuthorsByAuthorReportCount() =
        mongo.genderedAuthors.countDocuments(Author::authorReportUnprocessed eq true)

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

}
