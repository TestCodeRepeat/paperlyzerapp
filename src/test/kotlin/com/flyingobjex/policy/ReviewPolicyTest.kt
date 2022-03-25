package com.flyingobjex.policy

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.process.CoAuthorProcess
import com.flyingobjex.paperlyzer.process.ReviewPolicyProcess
import com.flyingobjex.paperlyzer.usecase.ReviewPolicyUseCase
import java.util.logging.Logger
import org.junit.Test

class ReviewPolicyTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)

    @Test
    fun `it should make all journal titles upppercase`(){
        val useCase = ReviewPolicyUseCase(mongo)
        useCase.updateJournalTitlesToUppercase()
    }

//    @Test
    fun `should have a review policy process`(){

    }

}
