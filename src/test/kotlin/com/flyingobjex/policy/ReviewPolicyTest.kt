package com.flyingobjex.policy

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.ReviewPolicyResponse
import com.flyingobjex.paperlyzer.usecase.ReviewPolicyUseCase
import io.kotest.matchers.shouldNotBe
import java.io.File
import java.util.logging.Logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test

class ReviewPolicyTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    val useCase = ReviewPolicyUseCase(mongo)


    @Test
    fun `should get stats`(){
        val res = useCase.printReviewPolicyStats()
        println("ReviewPolicyTest.kt :: should get stats() :: res = \n${res}")
        println("ReviewPolicyTest.kt :: should get stats() :: res = \n${res}")
    }

    @Test
    fun `it should apply policy to journals`() {
        useCase.applyJournalPolicyToPapers()
    }

    @Test
    fun `it should load review policies`() {
        val policyAsText = File("./review-policy.json").readText()
        val res = json.decodeFromString<ArrayList<ReviewPolicyResponse>>(policyAsText)
        res shouldNotBe null
    }

    //    @Test
    fun `it should make all journal titles upppercase`() {
        useCase.updateJournalTitlesToUppercase()
    }

    //    @Test
    fun `should have a review policy process`() {

    }

}
