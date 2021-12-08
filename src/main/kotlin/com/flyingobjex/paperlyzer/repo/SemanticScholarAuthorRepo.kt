package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.SemanticScholarAuthor
import com.flyingobjex.paperlyzer.entity.SemanticScholarPaper
import com.flyingobjex.paperlyzer.entity.SsAuthorDetails
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.process.SsAuthorProcessStats
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.*

class SemanticScholarAuthorRepo(val mongo: Mongo) {


    fun addAuthor(author: SemanticScholarAuthor) {
//        mongo
    }

    /** UNPROCESSED */
    fun getUnprocessedRawPapers(batchSize: Int): List<WosPaper> =
        mongo.rawPaperFullDetails.aggregate<WosPaper>(
            match(WosPaper::ssAuthorProcessedStep1 ne true),
            limit(batchSize)
        ).toList()

    fun getUnprocessedRawPapersCount(): Int =
        mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessedStep1 ne true).toInt()

    fun resetSsAuthorData() {
        mongo.ssAuthors.drop()
        mongo.rawPaperFullDetails.updateMany(
            WosPaper::ssAuthorProcessedStep1 ne false,
            listOf(
                setValue(WosPaper::ssAuthorProcessedStep1, false),
                setValue(WosPaper::ssAuthorProcessedStep2, false),
                setValue(WosPaper::ssAuthors, null)
            )
        )
    }

    fun getSsAuthorStats(): SsAuthorProcessStats {
        val totalRawPapersProcessed =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessedStep1 eq true).toInt()

        val totalRawPapersUnprocessed =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessedStep1 ne true).toInt()

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

    fun getSsPaperByWosDoi(doi: String): SemanticScholarPaper? =
        mongo.ssPapers.aggregate<SemanticScholarPaper>(
            match(SemanticScholarPaper::wosDoi eq doi)
        ).firstOrNull()

    fun upadteRawPaperWithSsAuthor(_id: String, ssAuthors: List<SsAuthorDetails>): UpdateResult =
        mongo.rawPaperFullDetails.updateOne(
            WosPaper::_id eq _id,
            listOf(
                setValue(WosPaper::ssAuthors, ssAuthors),
                setValue(WosPaper::ssAuthorProcessedStep1, true),
            )
        )
}
