package com.flyingobjex.tableBuilders

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.usecase.GenderedAuthorUseCase
import java.util.*
import java.util.logging.Logger

class BuildGenderedAuthorTableTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)
    private val genderedAuthorUseCase = GenderedAuthorUseCase(mongo)

    //    @Test
    fun `should copy author to new table & assign gender`() {
        println("${Date()} dbLive.clearGenderedAuthorsTable()")
        genderedAuthorUseCase.resetBuildGenderedAuthors()

        genderedAuthorUseCase.buildGenderedAuthorsTable(500000)
        println("${Date()} done -- should copy author to new table & assign gender")
    }


}
