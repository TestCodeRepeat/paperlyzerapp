package com.flyingobjex.sjr

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.process.SJRProcess
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import org.junit.Test

class SJRProcessTest {

    private val mongo = Mongo(false)
    private val wosRepo = WoSPaperRepository(mongo)

//    private val app = PaperlyzerApp(mongo)
    private val process = SJRProcess(mongo)

//    @Test
    fun `should reset SJR process by making all WOS paper sjrank = -5`(){
        process.reset()
    }

    @Test
    fun `should print stats for sjr process`(){
        process.printStats()
    }

//    @Test
    fun `should reset sjr process`(){
        process.reset()
        process.printStats()
    }

}
