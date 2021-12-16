package com.flyingobjex

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.control.MainCoordinator
import com.flyingobjex.paperlyzer.control.StatsController
import com.flyingobjex.paperlyzer.entity.Author
import com.flyingobjex.paperlyzer.entity.Gender
import com.flyingobjex.paperlyzer.entity.GenderIdentity
import com.flyingobjex.paperlyzer.repo.AuthorRepository
import com.flyingobjex.paperlyzer.repo.WoSPaperRepository
import com.flyingobjex.paperlyzer.usecase.AuthorTableUseCase
import com.flyingobjex.paperlyzer.usecase.GenderedAuthorUseCase
import org.litote.kmongo.div
import org.litote.kmongo.eq
import java.util.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class MainCoordinatorTestLiveData {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(true)
    private val samplePath = "../tbl_cli_full.tsv"
    private val coordinator = MainCoordinator(mongo, samplePath)
    private val paperRepo = WoSPaperRepository(mongo)
    private val stats = StatsController(mongo)

    private val authorRepo = AuthorRepository(mongo)
    private val genderedAuthorUseCase = GenderedAuthorUseCase(mongo)
    private val authorTableUseCase = AuthorTableUseCase(mongo)

//    @Test
    fun `apply genders to authors in paper`() {
        val resetTime = measureTimeMillis {
            paperRepo.resetPaperTableGenderInfo()
        }
        log.info("CoordinatorTestLiveData.apply genders to authors in paper()  resetTime = ${resetTime}" )

        val batchSize = 100000
        val res = coordinator.applyGendersToPaperTable(batchSize)
        print(res)

        log.info("CoordinatorTestLiveData.apply genders to authors in paper()  Date() = ${Date()} 0000" )
        coordinator.applyGendersToPaperTable(batchSize)
        log.info("CoordinatorTestLiveData.apply genders to authors in paper()  Date() = ${Date()} 1111" )
        coordinator.applyGendersToPaperTable(batchSize)
        log.info("CoordinatorTestLiveData.apply genders to authors in paper()  Date() = ${Date()} 2222" )
        coordinator.applyGendersToPaperTable(batchSize)
        log.info("CoordinatorTestLiveData.apply genders to authors in paper()  Date() = ${Date()} 3333" )
        coordinator.applyGendersToPaperTable(batchSize)

    }

//        @Test
    fun `build a raw journal table`(){
        coordinator.resetJournalTable()
        coordinator.buildJournalTable()
        val stats = stats.statsJournalTable()
        assertEquals(22273, stats.totalJournals)
    }


//    @Test
    fun `build author table from raw author table`() {
        val resetTime = measureTimeMillis {
            authorTableUseCase.resetForAuthorTable()
        }
        log.info("CoordinatorTest.extract authors from raw papers into author table()  resetTime = $resetTime")

        val parseTime = measureTimeMillis {
            authorRepo.buildAuthorTableInParallel(2000000)
        }
        log.info("CoordinatorTest.build author table from raw author table()  parseTime = $parseTime")

        val rawAuthorsUpdated = mongo.rawAuthors.countDocuments(Author::duplicateCheck eq true)
        println("rawAuthorsUpdated = $rawAuthorsUpdated")

        val rawAuthorsPending = mongo.rawAuthors.countDocuments(Author::duplicateCheck eq false)
        println("rawAuthorsUpdated = $rawAuthorsPending")

        val totalAuthors = mongo.authors.countDocuments(Author::gender / Gender::gender eq GenderIdentity.UNASSIGNED)
        println("totalAuthors = $totalAuthors")
        assertEquals(444875, totalAuthors)
    }

//    @Test
    fun `LIVE DATA !!! extract authors from raw papers into author table`() {
        val resetTime = measureTimeMillis {
            coordinator.resetForBuildRawAuthorTable()
        }
        log.info("CoordinatorTest.extract authors from raw papers into author table()  resetTime = $resetTime")

        val parseTime = measureTimeMillis {
            val res = coordinator.runParseRawAuthorTableFromRawPapers()
            assertEquals(1734039, res.size)
        }
        log.info("CoordinatorTest.extract authors from raw papers into author table()  parseTime = $parseTime")
    }

//    @Test
    fun `LIVE DATA !!! parse initial csv file into paper table`() {
        val clearTime = measureTimeMillis {
            paperRepo.clearRawPapers()
        }
        log.info("CoordinatorTest.parse initial csv file into paper table()  clearTime = $clearTime")

        val parseTime = measureTimeMillis {
            val res = coordinator.runParseCsvToRawPapers()
            assertEquals(399893, res.size)
        }
        log.info("CoordinatorTest.parse initial csv file into paper table()  parseTime = $parseTime")
    }
}
