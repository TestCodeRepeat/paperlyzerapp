package com.flyingobjex.builders

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.Stats
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import org.junit.Test
import java.io.File
import java.util.logging.Logger
import kotlin.system.measureTimeMillis

class BuildCSVReportTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(true)
    private val samplePath = "../tbl_cli_sample.tsv"
    private val paperRepo = WoSPaperRepository(mongo)
    private val dbLive = Mongo(true)
    private val repo = AuthorRepository(dbLive)
    private val stats = Stats(mongo)

    @Test
    fun `update report in database`(){
//        stats.resetDisciplinesReport()
//        stats.runPapersWithDisciplinesReport()
        log.info("BuildCSVReportTest.update report in database()  done!")
    }

//    @Test
    fun `build general stats report`(){
        val globalStats = stats.runGlobalStatsReport()
        println(globalStats)
        log.info("BuildCSVReportTest.build general stats report()  globalStats = " )
    }

//    @Test
    fun `generate gendered paper csv report`(){
        val res = stats.runGenderedPaperReport()
        File("../PaperlyzerReports/genderedPapers.csv").writeBytes(res.readBytes())
    }

//    @Test
    fun `generate gendered author csv report`() {
        val buildTime = measureTimeMillis {
            val res = stats.runGenderedAuthorReport()

            File("../PaperlyzerReports/genderedAuthorTable.csv").writeBytes(res.readBytes())
//            File("src/test/resources/genderReveal.csv").writeBytes(res.readBytes())
            println(res)
        }
        log.info("BuildCSVReportTest.generate csv report()  buildTime = ${buildTime}" )


    }
}
