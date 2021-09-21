package com.flyingobjex.builders

import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.Mongo
import org.junit.Test
import java.util.*

class BuildFirstNamesTableTest {

    private val testName = "Chang Xinyu"
    private val dbLive = Mongo(true)
    private val repo = AuthorRepository(dbLive)

    @Test
    fun `should build first name table`() {
        println("${Date()} dbLive.clearFirstNameTable()")
//        dbLive.clearFirstNameTable()
        println("${Date()} repo.buildFirstNameTable()")
//        repo.buildFirstNameTable()
        println("done")
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