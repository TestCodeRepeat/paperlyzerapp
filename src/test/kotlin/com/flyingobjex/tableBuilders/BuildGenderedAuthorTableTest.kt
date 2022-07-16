package com.flyingobjex.tableBuilders

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.usecase.GenderedAuthorUseCase
import java.util.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals
import org.junit.Test

class BuildGenderedAuthorTableTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)
    private val genderedAuthorUseCase = GenderedAuthorUseCase(mongo)

    @Test
    fun `should copy author to new table & assign gender`() {
        println("${Date()} dbLive.clearGenderedAuthorsTable()")
        genderedAuthorUseCase.resetBuildGenderedAuthors()

        println("${Date()} done -- should copy author to new table & assign gender")

        val first = measureTimeMillis {
            genderedAuthorUseCase.buildGenderedAuthorsTable(800000)
        }
        log.info("BuildGenderedAuthorTableTest.should copy author to new table & assign gender() first  = ${first}" )

        val res = genderedAuthorUseCase.statsGenderedAuthorsTable()
        println("BuildGenderedAuthorTableTest.kt :: STATS = $res")
    }
}
