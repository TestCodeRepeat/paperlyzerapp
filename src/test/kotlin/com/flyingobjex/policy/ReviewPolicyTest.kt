package com.flyingobjex.policy

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.PaperlyzerApp
import com.flyingobjex.paperlyzer.process.CoAuthorProcess
import com.flyingobjex.paperlyzer.process.ReviewPolicyProcess
import java.util.logging.Logger
import org.junit.Test

class ReviewPolicyTest {

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)

    private val process = ReviewPolicyProcess(mongo)
    private val app = PaperlyzerApp(mongo, process)

    @Test
    fun `should have a review policy process`(){

    }

}
