package com.flyingobjex.tableBuilders

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.usecase.GenderedPaperUseCase
import java.util.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import org.junit.Test

class BuildGenderedPaperTableTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)

    private val genderedPaperUseCase = GenderedPaperUseCase(mongo)


    //    @Test
    fun `build gendered papers table`() {
        val resetTime = measureTimeMillis {
            genderedPaperUseCase.resetGenderedPaperTable()
        }
        log.info("CoordinatorTestLiveData.apply genders to authors in paper()  resetTime = ${resetTime}")

        val batchSize = 100000
        val res = genderedPaperUseCase.applyGendersToPaperTable(batchSize)
        print(res)

        log.info("CoordinatorTestLiveData.apply genders to authors in paper()  Date() = ${Date()} 0000")
        genderedPaperUseCase.applyGendersToPaperTable(batchSize)
        log.info("CoordinatorTestLiveData.apply genders to authors in paper()  Date() = ${Date()} 1111")
        genderedPaperUseCase.applyGendersToPaperTable(batchSize)
        log.info("CoordinatorTestLiveData.apply genders to authors in paper()  Date() = ${Date()} 2222")
        genderedPaperUseCase.applyGendersToPaperTable(batchSize)
        log.info("CoordinatorTestLiveData.apply genders to authors in paper()  Date() = ${Date()} 3333")
        genderedPaperUseCase.applyGendersToPaperTable(batchSize)

    }

    @Test
    fun `should generate a small batch of gendered papers`() {
        val resetTime = measureTimeMillis {
            genderedPaperUseCase.resetGenderedPaperTable()
        }

        val res = genderedPaperUseCase.applyGendersToPaperTable(100)
        print(res)
        genderedPaperUseCase.printStats()
    }
}
