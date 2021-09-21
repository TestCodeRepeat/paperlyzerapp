package com.flyingobjex.builders

import com.flyingobjex.paperlyzer.api.GenderApi
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.repo.GenderRepo
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

class HyphenatedNameApiBatchTest {

    private val testName = "Abbas-Ali"
    private val dbLive = Mongo(true)
    private val genderRepo = GenderRepo(dbLive)


//    @Test
    fun `run continuous gender api calls`() {
        runBlocking {
            genderRepo.runContinuousGenderApi()
        }
        print("!! done !!")
    }

//    @Test
    fun `find remaining first name records to run against api`() {
        val remaining = genderRepo.remainingRecordsToRun()
        print(remaining)

    }

    //    @Test
    fun `should run small batch of hypenated names against API`() {
        val batchSize = 50
        runBlocking {
            genderRepo.runBatchAgainstGenderApi(batchSize)
        }
    }

    //    @Test
    fun `reset hyphenated first names table`() {
        val res = genderRepo.resetHyphenatedInFirstNamesTable()
        assertEquals(0, res.size)
    }

    //    @Test
    fun `running a hyphenated name against the gender api should get a result`() {
        runBlocking {
            val res = genderRepo.genderApi.fetchGenderByFirstname("Abbas-Ali")
            println(res)
            println(res)
        }
    }
}
