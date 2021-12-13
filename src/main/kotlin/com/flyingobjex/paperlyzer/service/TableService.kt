package com.flyingobjex.paperlyzer.service

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import java.util.*

class TableService(val mongo: Mongo) {

    private val authorRepo = AuthorRepository(mongo)

    /** First Names Table */
    fun buildFirstNamesTable() {
        val batchSize = 2000000
        println("${Date()} repo.buildFirstNameTable() 0000")
        authorRepo.buildFirstNameTable(batchSize)
        println("done")
    }
}
