package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Journal
import com.flyingobjex.paperlyzer.entity.JournalTextType
import org.litote.kmongo.*
import java.util.logging.Logger

class JournalTableRepo(val mongo: Mongo) {

    val log: Logger = Logger.getAnonymousLogger()

    fun buildJournalTableInParallel(batchSize: Int) {
        val batch = mongo.rawPaperFullDetails.find().limit(batchSize).toList()

        log.info("JournalTableRepo.buildJournalTableInParallel()  batch.size = ${batch.size}")
        batch.parallelStream().forEach { fullPaperDetail ->

            mongo.journals.findOne(
                Journal::journalName eq fullPaperDetail.journal,
                Journal::doi eq fullPaperDetail.doi
            )?.let { matched ->
                val shortTitles = mutableListOf(fullPaperDetail.journal)
                shortTitles.add(fullPaperDetail.journal)
                matched.shortTitles = shortTitles.toList()
                matched.citationCount = matched.citationCount++

                mongo.journals.updateOne(target = matched)
            } ?: run {
                val journal = Journal(
                    fullPaperDetail.journal,
                    JournalTextType.Article.toType(fullPaperDetail.text_type) ?: JournalTextType.NA,
                    fullPaperDetail.doi,
                    listOf(fullPaperDetail.shortTitle),
                    1,
                    fullPaperDetail.keywords.split(";"),
                    fullPaperDetail.topics,
                )
                mongo.journals.insertOne(journal)
            }
        }
    }
}
