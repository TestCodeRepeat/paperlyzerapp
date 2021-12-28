package com.flyingobjex.report

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.process.reports.AuthorReportProcess
import org.junit.Test

class AuthorReportProcessTest {

    private val mongo = Mongo(false)
    private val process = AuthorReportProcess(mongo)
    private val app = PaperlyzerApp(mongo, process)

    @Test
    fun `should run Author Report process`() {
        println("ReportProcessTest.kt :: should start running processes :: 0000")
        app.process.reset()
        println("ReportProcessTest.kt :: should start running processes :: 1111")
        app.process.printStats()
        println("ReportProcessTest.kt :: should start running processes :: 2222")
        app.start()
        println("ReportProcessTest.kt :: should start running processes :: DONE")
    }

    //    @Test
    fun `should reset and report process once`() {
        process.reset()
        process.runProcess()
    }


}
