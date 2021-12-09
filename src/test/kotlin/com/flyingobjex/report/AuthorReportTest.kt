package com.flyingobjex.report

import com.flyingobjex.process.coauthors.verifyProcessType
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.ProcessType
import org.junit.Test

class AuthorReportTest {
    private val mongo = Mongo(false)


    private val app = PaperlyzerApp(mongo)
//    private val process = PaperReportProcess(mongo)

    @Test
    fun `app should run Author Report Process`() {
        val processType = app.process.type()
        verifyProcessType(processType, ProcessType.AuthorReport)
        app.process.printStats()
        app.process.reset()
        app.process.printStats()
        app.start()
        app.process.printStats()
    }
}
