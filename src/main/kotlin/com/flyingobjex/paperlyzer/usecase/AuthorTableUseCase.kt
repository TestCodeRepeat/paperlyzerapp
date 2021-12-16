package com.flyingobjex.paperlyzer.usecase

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import java.util.*

class AuthorTableUseCase(val mongo: Mongo) {

    val authorRepo = AuthorRepository(mongo)

    fun resetForAuthorTable() {
        println("${Date()} clearAuthors()")
        mongo.clearAuthors()
        println("${Date()} resetRawAuthors()")
        authorRepo.resetRawAuthors()
        mongo.resetIndexes()

    }

    fun buildAuthorTableFromRawAuthorTable(batchSize:Int){
        authorRepo.buildAuthorTableInParallel(batchSize)
    }
}
