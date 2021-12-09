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


    /** UPDATE */
    fun updatePaper(paper: WosPaper) = mongo.rawPaperFullDetails.updateOne(paper)

    // STEP 1
    fun updateRawPaperWithSsAuthor(_id: String, ssAuthors: List<SsAuthorDetails>? = null): UpdateResult =
        mongo.rawPaperFullDetails.updateOne(
            WosPaper::_id eq _id,
            listOf(
                setValue(WosPaper::ssAuthors, ssAuthors),
                setValue(WosPaper::ssAuthorProcessedStep1, true),
            )
        )

    /** GETTERS */
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
        mongo.rawPaperFullDetails.aggregate<WosPaper>(WosPaper::ssAuthorProcessedStep2 ne true).toList()

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
}
