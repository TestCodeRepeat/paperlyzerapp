package com.flyingobjex.paperlyzer.repo

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.SemanticScholarPaper
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

typealias SsPaper = SemanticScholarPaper

class SemanticScholarPaperRepository(val mongo: Mongo) {

    /** WIP - apply Semantic Scholar published years to genered author table  */
    fun papersForAuthor(author: Author?): List<SemanticScholarPaper> {
        return emptyList()
    }

    fun insertPaper(paper: SemanticScholarPaper, outgoing: SendChannel<Frame>? = null) {
        try {
            mongo.ssPapers.insertOne(paper)
        } catch (e: Exception) {
            print("unable to insert paper ${paper.doi}")
            GlobalScope.launch {
                outgoing?.send(Frame.Text("unable to insert paper ${paper.doi}"))
            }
        }
    }

    fun getAllSsPapers(): List<SemanticScholarPaper> {
        return mongo.ssPapers.find().toList()
    }

    fun paperByDoi(doi: String): SemanticScholarPaper? =
        mongo.ssPapers.findOne(SsPaper::wosDoi eq doi)


}
