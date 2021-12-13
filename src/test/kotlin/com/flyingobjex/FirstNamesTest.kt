package com.flyingobjex

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.MainCoordinator
import com.flyingobjex.paperlyzer.control.StatsController
import com.flyingobjex.paperlyzer.service.TableService
import java.util.*
import java.util.logging.Logger
import org.junit.Test

class FirstNamesTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)
    val service = TableService(mongo)
    private val stats = StatsController(mongo)


    @Test
    fun `should build a first names table from WoS Papers`() {
        mongo.clearFirstNameTable()
        println("${Date()} dbLive.clearFirstNameTable()")
        service.buildFirstNamesTable()
        log.info("CoordinatorTest.build first names table from author's table()  res = ")

        val stats = stats.firstNamesTableTotalNames()
        log.info("CoordinatorTest.build first names table from author's table()  stats = $stats")


    }
}
