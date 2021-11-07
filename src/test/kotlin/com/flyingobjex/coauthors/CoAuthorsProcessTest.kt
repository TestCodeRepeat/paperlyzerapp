package com.flyingobjex.coauthors

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.process.CoAuthorProcess
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import org.junit.Test

class CoAuthorsProcessTest {
    private val mongo = Mongo(false)
    private val wosRepo = WoSPaperRepository(mongo)
    private val authorRepo = AuthorRepository(mongo)

    private val process = CoAuthorProcess(mongo)
    private val app = PaperlyzerApp(mongo)


    @Test
    fun `app should run coauthor proces`(){
        app.process.printStats()
        app.process.reset()
        app.process.printStats()
        app.start()
        app.process.printStats()


    }
//    @Test
    fun `should run coauthor process`(){
        process.printStats()
        process.runProcess()
        process.printStats()
    }

//    @Test
    fun `should reset co authors`(){
        process.printStats()
        process.reset()
        process.printStats()
    }

}
