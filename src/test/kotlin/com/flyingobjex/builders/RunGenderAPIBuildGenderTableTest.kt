package com.flyingobjex.builders

import com.flyingobjex.paperlyzer.api.GenderApi
import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.api.GENDER_API_KEY
import com.flyingobjex.paperlyzer.repo.GenderRepo
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.json
import kotlin.test.assertTrue
import org.junit.Test

class RunGenderAPIBuildGenderTableTest {

    private val testName = "Karen"
    private val dbLive = Mongo(true)
    private val repo = GenderRepo(dbLive)
    private val genderApi = GenderApi(GENDER_API_KEY)

    @Test
    fun `should get gender from API`() =
        runBlocking {
            val res = genderApi.fetchGender(testName)
            println(res.json)
            assertTrue(res.gender == "female")
        }

    //    @Test
    fun `run continuous batch on gender api`() {
        runBlocking {
            repo.runContinuousGenderApi()
        }
    }

    //    @Test
    fun `clear gender table & reset firstNames table`() {
//        dbLive.resetFirstNameTable()
//        dbLive.clearGenderTable()
    }

    //    @Test
    fun `should return a batch of matching Genders from API, searching on first names`() {
        runBlocking {
//            dbLive.resetFirstNameTable()
//            dbLive.clearGenderTable()
//            repo.runBatchAgainstGenderApi()
            println("done")
        }
    }

    //        @Test
    fun `should fetch gender by first name`() {
        runBlocking {
            val res = genderApi.fetchGenderByFirstname("Jamie")
            println(res)
            println(res)
        }
    }


    //    @Test
    fun `should fetch gender by full name`() {
        runBlocking {
            val res = genderApi.fetchGenderByFullName("Jamie", "Wilson")
            println(res)
            println(res)
        }
    }


}
