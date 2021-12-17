package com.flyingobjex.process.sjr

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.process.SJRProcess
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import io.kotest.matchers.shouldBe
import org.junit.Test

class SJRProcessTest {

    private val mongo = Mongo(false)
    private val wosRepo = WoSPaperRepository(mongo)


    private val process = SJRProcess(mongo)
    private val app = PaperlyzerApp(mongo, process)

//    @Test
    fun `app should run sjr process`(){
        app.process.type() shouldBe ProcessType.SJR
        app.process.reset()
        app.process.printStats()
        app.start()
        app.process.printStats()
    }

//    @Test
    fun `should run SJR process for first 10000 records`(){
        process.reset()
        process.runProcess()
        process.printStats()
    }

//    @Test
    fun `should print stats for sjr process`(){
        process.printStats()
    }

//    @Test
    fun `should reset sjr process`(){
        process.reset()
        process.printStats()
    }

}
