package com.flyingobjex.builders

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.MainCoordinator
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import org.junit.Test
import java.util.*

class BuildGenderedAuthorsTable {
    private val mongo = Mongo(true)
    private val samplePath = "../tbl_cli_full.tsv"
    private val coordinator = MainCoordinator(mongo, samplePath)
    private val authorTableRepo = AuthorRepository(mongo)


    @Test
    fun `should copy author to new table & assign gender`(){
        println("${Date()} dbLive.clearGenderedAuthorsTable()")
        mongo.clearGenderedAuthorsTable()
        println("${Date()} dbLive.resetIndexes()")
        mongo.resetIndexes()
        println("${Date()} repo.buildGenderedAuthorsTable()")
        coordinator.buildGenderedAuthorsTable(500000)
        println("${Date()} done -- should copy author to new table & assign gender")
    }
}
