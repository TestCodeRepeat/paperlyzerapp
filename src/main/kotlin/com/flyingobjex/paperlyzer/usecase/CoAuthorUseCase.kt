package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.process.CoAuthorProcessStats
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.*

class CoAuthorUseCase(val mongo: Mongo) {


    fun resetCoAuthorData() {
        mongo.genderedAuthors.updateMany(
            Author::averageCoAuthors ne -5.5,
            listOf(
                setValue(Author::averageCoAuthors, -5.5),
                setValue(Author::totalPapers, -5)
            )
        )
    }

    fun getCoAuthorStats(): CoAuthorProcessStats {
        val totalAuthors = mongo.genderedAuthors.countDocuments()
        val totalProcessed = mongo.genderedAuthors.countDocuments(Author::averageCoAuthors gt -5.0)
        return CoAuthorProcessStats(
            totalProcessedWithCoAuthorData = totalProcessed,
            totalUnprocessed = unprocessedCoAuthorsCount(),
            totalAuthors = totalAuthors,
            0
        )
    }

    fun unprocessedCoAuthorsCount(): Long = mongo.genderedAuthors.countDocuments(Author::averageCoAuthors eq -5.5)

    fun getUnprocessedAuthorsByCoAuthors(batchSize: Int): List<Author> {
        return mongo.genderedAuthors.aggregate<Author>(
            match(Author::averageCoAuthors eq -5.5),
            limit(batchSize)
        ).toList()
    }

    fun updateAuthorCoAuthors(author: Author): UpdateResult {
        return mongo.genderedAuthors.updateOne(
            Author::_id eq author._id,
            listOf(
                setValue(Author::totalPapers, author.totalPapers),
                setValue(Author::averageCoAuthors, author.averageCoAuthors),
                setValue(Author::firstAuthorCount, author.firstAuthorCount),
                setValue(Author::averageGenderRatioOfPapers, author.averageGenderRatioOfPapers),
                setValue(Author::genderRatioOfAllCoAuthors, author.genderRatioOfAllCoAuthors),

                )
        )
    }

}
