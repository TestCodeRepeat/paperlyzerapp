package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.parser.DisciplineType
import com.flyingobjex.paperlyzer.process.StemSshAuthorProcessStats
import org.litote.kmongo.*

class StemSshUseCase(val mongo: Mongo) {

    fun getStemSshAuthorStats(): StemSshAuthorProcessStats {
        val totalAuthors = mongo.genderedAuthors.countDocuments()
        val totalProcessed = mongo.genderedAuthors.countDocuments(Author::discipline ne DisciplineType.UNINITIALIZED)
        val totalStem = mongo.genderedAuthors.countDocuments(Author::discipline eq DisciplineType.STEM)
        val totalSsh = mongo.genderedAuthors.countDocuments(Author::discipline eq DisciplineType.SSH)
        val totalM = mongo.genderedAuthors.countDocuments(Author::discipline eq DisciplineType.M)
        val totalUnidentified = mongo.genderedAuthors.countDocuments(Author::discipline eq DisciplineType.NA)

        return StemSshAuthorProcessStats(
            totalProcessedWithStemSshData = totalProcessed,
            totalUnprocessed = getUnprocessedAuthorsByAStemSshCount(),
            totalStem = totalStem,
            totalSsh = totalSsh,
            totalM = totalM,
            totalUnidentified = totalUnidentified,
            totalAuthors = totalAuthors,
        )
    }

    fun getUnprocessedAuthorsByStemSsh(batchSize: Int): List<Author> {
        val res = mongo.genderedAuthors.aggregate<Author>(
            match(Author::disciplineScore eq -5.5),
            limit(batchSize)
        ).toList()

        return res
    }

    fun getUnprocessedAuthorsByAStemSshCount(): Long =
        mongo.genderedAuthors.countDocuments(Author::discipline eq DisciplineType.UNINITIALIZED)

    fun resetStemSsh() {
        mongo.genderedAuthors.updateMany(
            Author::discipline ne DisciplineType.UNINITIALIZED,
            listOf(
                setValue(Author::disciplineScore, -5.5),
                setValue(Author::discipline, DisciplineType.UNINITIALIZED)
            )
        )
    }

}
