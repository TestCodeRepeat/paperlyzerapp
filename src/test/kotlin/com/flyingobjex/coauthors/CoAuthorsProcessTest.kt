package com.flyingobjex.coauthors

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.process.CoAuthorProcess
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import org.junit.Test

class CoAuthorsProcessTest {
    private val mongo = Mongo(false)
    private val wosRepo = WoSPaperRepository(mongo)
    private val authorRepo = AuthorRepository(mongo)

    private val process = CoAuthorProcess(mongo)

    @Test
    fun `should reset co authors`(){
        process.printStats()
        process.reset()
        process.printStats()
    }

}
