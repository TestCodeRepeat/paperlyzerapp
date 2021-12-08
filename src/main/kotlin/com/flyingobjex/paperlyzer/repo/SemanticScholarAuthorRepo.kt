package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.SemanticScholarAuthor
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.process.SsAuthorProcessStats
import org.litote.kmongo.*

class SemanticScholarAuthorRepo(val mongo: Mongo) {


    fun addAuthor(author: SemanticScholarAuthor) {
//        mongo
    }

    /** UNPROCESSED */
    fun getUnprocessedRawPapers(batchSize: Int): List<WosPaper> =
        mongo.rawPaperFullDetails.aggregate<WosPaper>(
            match(WosPaper::ssAuthorProcessed ne true),
            limit(batchSize)
        ).toList()

    fun getUnprocessedRawPapersCount(): Int =
        mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessed ne true).toInt()

    fun resetSsAuthorData() {
        mongo.ssAuthors.drop()
        mongo.rawPaperFullDetails.updateMany(
            WosPaper::ssAuthorProcessed ne false,
            setValue(WosPaper::ssAuthorProcessed, false)
        )
    }

    fun getSsAuthorStats(): SsAuthorProcessStats {
        val totalRawPapersProcessed =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessed eq true).toInt()

        val totalRawPapersUnprocessed =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessed ne true).toInt()

        val totalWosPapers =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::title ne null).toInt()

        val totalSsAuthorsFound =
            mongo.ssAuthors.countDocuments(SemanticScholarAuthor::authorId ne null).toInt()

        return SsAuthorProcessStats(
            totalRawPapersProcessed = totalRawPapersProcessed,
            totalRawPapersUnprocessed = totalRawPapersUnprocessed,
            totalUnidentified = -5,
            totalWosPapers = totalWosPapers,
            totalSsAuthorsFound = totalSsAuthorsFound,
        )
    }
}
