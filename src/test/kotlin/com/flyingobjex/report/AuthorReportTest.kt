package com.flyingobjex.report

import com.flyingobjex.coauthors.verifyProcessType
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.ProcessType
import com.flyingobjex.paperlyzer.process.reports.PaperReportProcess
import org.junit.Test

class AuthorReportTest {
    private val mongo = Mongo(false)


    private val app = PaperlyzerApp(mongo)
//    private val process = PaperReportProcess(mongo)

    @Test
    fun `app should run Author Report Process`() {
        val processType = app.process.type()
        verifyProcessType(processType, ProcessType.authorReport)
        app.process.printStats()
        app.process.reset()
        app.process.printStats()
        app.start()
        app.process.printStats()
    }
}
