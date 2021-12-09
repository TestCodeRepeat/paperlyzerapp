package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.SemanticScholarAuthor
import com.flyingobjex.paperlyzer.entity.SemanticScholarPaper
import com.flyingobjex.paperlyzer.entity.SsAuthorDetails
import com.flyingobjex.paperlyzer.entity.WosPaper
import com.flyingobjex.paperlyzer.process.SsApiAuthorDetailsStats
import com.flyingobjex.paperlyzer.process.SsAuthorProcessStats
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.*

class SemanticScholarAuthorRepo(val mongo: Mongo) {

    /** INSERT */
    fun addSsAuthorDetails(ssAuthorDetails: SemanticScholarAuthor): InsertOneResult =
        mongo.ssAuthors.insertOne(ssAuthorDetails)

    /** UPDATE */
    fun updatePaper(paper: WosPaper) = mongo.rawPaperFullDetails.updateOne(paper)

    // STEP 1
    fun updateRawPaperWithSsAuthorStep1(_id: String, ssAuthors: List<SsAuthorDetails>? = null): UpdateResult =
        mongo.rawPaperFullDetails.updateOne(
            WosPaper::_id eq _id,
            listOf(
                setValue(WosPaper::ssAuthors, ssAuthors),
                setValue(WosPaper::ssAuthorProcessedStep1, true),
            )
        )

    // STEP 2s
    fun updateRawPaperWithSsAuthorStep2(_id: String?): UpdateResult =
        mongo.rawPaperFullDetails.updateOne(
            WosPaper::_id eq _id,
            setValue(WosPaper::ssAuthorProcessedStep2, true),
        )

    /** GETTERS */
    // STEP 1
    fun getSsPaperByWosDoi(doi: String): SemanticScholarPaper? =
        mongo.ssPapers.findOne(SemanticScholarPaper::wosDoi eq doi)


    /** STATS */
    // Step 1
    fun getRawPaperWithSsAuthorStats(): SsAuthorProcessStats {
        val totalRawPapersProcessed =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessedStep1 eq true).toInt()

        val totalRawPapersUnprocessed =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessedStep1 ne true).toInt()

        val totalWosPapers =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::title ne null).toInt()

//        val totalSsAuthorsFound =
//            mongo.ssAuthors.countDocuments(SemanticScholarAuthor::authorId ne null).toInt()

        val totalUnidentified =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthors eq emptyList<SsAuthorDetails>()).toInt()

        return SsAuthorProcessStats(
            totalRawPapersProcessed = totalRawPapersProcessed,
            totalRawPapersUnprocessed = totalRawPapersUnprocessed,
            totalUnidentified = totalUnidentified,
            totalWosPapers = totalWosPapers,
        )
    }

    // STEP 2
    fun getSsApiAuthorDetailsStats(): SsApiAuthorDetailsStats {

        val totalRawPapersProcessed =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessedStep2 eq true).toInt()

        val totalRawPapersUnprocessed =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessedStep2 ne true).toInt()

        val totalSsAuthorsFound = mongo.ssAuthors.countDocuments().toInt()

        val totalWosPapers =
            mongo.rawPaperFullDetails.countDocuments(WosPaper::title ne null).toInt()

        return SsApiAuthorDetailsStats(
            totalRawPapersProcessed = totalRawPapersProcessed,
            totalRawPapersUnprocessed = totalRawPapersUnprocessed,
            totalSsAuthorsFound = totalSsAuthorsFound,
            totalUnidentified = -5,
            totalWosPapers = totalWosPapers
        )
    }


    /** UNPROCESSED */
    // STEP 1
    fun getUnprocessedRawPapers(batchSize: Int): List<WosPaper> =
        mongo.rawPaperFullDetails.aggregate<WosPaper>(
            match(WosPaper::ssAuthorProcessedStep1 ne true),
            limit(batchSize)
        ).toList()

    fun getUnprocessedRawPapersCount(): Int =
        mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessedStep1 ne true).toInt()

    // STEP 2
    fun getUnprocessedRawPapersBySsAuthorDetails(batchSize: Int): List<WosPaper> =
        mongo.rawPaperFullDetails.aggregate<WosPaper>(
            match(WosPaper::ssAuthorProcessedStep2 ne true),
            limit(batchSize)
        ).toList()

    fun getUnprocessedRawPapersBySsAuthorDetailsCount(): Int =
        mongo.rawPaperFullDetails.countDocuments(WosPaper::ssAuthorProcessedStep2 ne true).toInt()


    /** RESET */
    fun resetSsAuthorDataStep1() {
        mongo.ssAuthors.drop()
        mongo.rawPaperFullDetails.updateMany(
            WosPaper::ssAuthorProcessedStep1 ne false,
            listOf(
                setValue(WosPaper::ssAuthorProcessedStep1, false),
                setValue(WosPaper::ssAuthorProcessedStep2, false),
                setValue(WosPaper::wosPaperAuthorCount, null),
                setValue(WosPaper::ssPaperAuthorCount, null),
                setValue(WosPaper::authorCountDifference, null),
                setValue(WosPaper::ssAuthors, null)
            )
        )
    }

    fun resetSsAuthorDataStep2() {
        mongo.ssAuthors.drop()
        mongo.rawPaperFullDetails.updateMany(
            WosPaper::ssAuthorProcessedStep2 ne false,
            setValue(WosPaper::ssAuthorProcessedStep2, false),
        )
    }

}
