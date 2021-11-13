package com.flyingobjex.report

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.parser.CSVTopicParser
import com.flyingobjex.paperlyzer.parser.TopicMatcher
import com.flyingobjex.paperlyzer.process.reports.PaperReportProcess
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import kotlinx.serialization.json.Json
import org.junit.Test

class PaperReportProcessTest {
    private val mongo = Mongo(false)
    private val wosRepo = WoSPaperRepository(mongo)

    private val samplePath = "../../topics.tsv"
    private val topics = CSVTopicParser.csvFileToTopicList(samplePath)
    private val testTopicA = "Metallurgy & Metallurgical Engineering"
    private val testTopicB = "Geochemistry & Geophysics"

    private val format = Json { prettyPrint = true }
    private val matcher = TopicMatcher(topics)

    private val app = PaperlyzerApp(mongo)
    private val process = PaperReportProcess(mongo)

    @Test
    fun `should start running processes`(){
        println("ReportProcessTest.kt :: should start running processes :: 0000")
        app.process.reset()
        println("ReportProcessTest.kt :: should start running processes :: 1111")
        app.process.printStats()
        println("ReportProcessTest.kt :: should start running processes :: 2222")
        app.start()
        println("ReportProcessTest.kt :: should start running processes :: DONE")
    }

//    @Test
    fun `should reset and report process once`(){
        process.reset()
        process.runProcess()
    }


}
