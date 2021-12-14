package com.flyingobjex.tableBuilders

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.StatsController
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.usecase.FirstNamesUseCase
import io.kotest.mpp.timeInMillis
import java.util.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import org.intellij.lang.annotations.JdkConstants
import org.junit.Test

class BuildFirstNamesTableTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val testName = "Chang Xinyu"
    private val mongo = Mongo(true)
    private val repo = AuthorRepository(mongo)
    private val statsController = StatsController(mongo)
    private val firstNamesUseCase = FirstNamesUseCase(mongo)

    //    @Test
    fun `should add to first names table from SS Author table`() {
        val time = measureTimeMillis {
            firstNamesUseCase.buildFirstNameTableFromSsAuthorTable(500000)
            println("done")
        }
    }


    //    @Test
    fun `should build first name table from WoS Authors table`() {
        println("${Date()} dbLive.clearFirstNameTable()")
        mongo.clearFirstNameTable()
        println("${Date()} repo.buildFirstNameTable()")
        val time = measureTimeMillis {
            firstNamesUseCase.buildFirstNameTable(500000)
            println("done")
        }

        log.info("BuildFirstNamesTableTest.should build first name table()  time = ${time}")
    }

    //    @Test
    fun `reset author table`() {

        println("done")
    }

}

val fullNames = listOf(
    "Murat Mahkamov",
    "Craig A. Zinke",
    "Laima Miseckaite",
    "Jian Li",
    "Gudeta W. Njoloma",
    "Jarkko Saeporsdottir",
    "Natacha Mosha",
    "Maria Z. Schaer",
    "Jean-Olivier Sorichetti",
    "Daniele Calanca",
    "Ashutosh Verma",
    "Christian M. Saarinen",
    "Ortrud McGuire",
    "Ryan J. Allan",
    "Tomoya Huang",
    "Ombir Bhat",
    "Javier Izcaro",
    "Silvina Nunez",
    "Ailene K. Lundquist",
    "Douglas Padmanaba",
)

val firstNames = listOf(
    "Murat",
    "Craig",
    "Laima",
    "Jian",
    "Gudeta",
    "Jarkko",
    "Natacha",
    "Maria",
    "Jean",
    "Daniele",
    "Ashutosh",
    "Christian",
    "Ortrud",
    "Ryan",
    "Tomoya",
    "Ombir",
    "Javier",
    "Silvina",
    "Ailene",
    "Douglas",
)
