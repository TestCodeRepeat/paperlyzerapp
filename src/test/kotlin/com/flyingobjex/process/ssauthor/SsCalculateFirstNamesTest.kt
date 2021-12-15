package com.flyingobjex.process.ssauthor

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.usecase.FirstNamesUseCase
import com.flyingobjex.paperlyzer.util.StringUtils
import com.flyingobjex.paperlyzer.util.StringUtils.aliasToLongestLastName
import com.flyingobjex.paperlyzer.util.StringUtils.aliasToLongestMiddleName
import com.flyingobjex.paperlyzer.util.StringUtils.aliasesToLongestFirstName
import io.kotest.matchers.shouldBe
import java.util.logging.Logger
import org.junit.Test

class SsCalculateFirstNamesTest {

    val mongo = Mongo()
    val firstNamesUseCase = FirstNamesUseCase(mongo)
    val log: Logger = Logger.getAnonymousLogger()

    val aliases = listOf(
        "R K P",
        "Roberta Kip Pernetti",
        "Rob Kippington P.",
        "R. Pernetti",
    )

    val jCook = listOf(
        "C J Cook",
        "C. J. Cook",
        "Chris Cook",
    )

    val fourNames = listOf(
        "P J Harvey Wallbanger",
        "Pretoria James H. Wallbanger",
        "Pamela Jons Harvey Wally",
    )

    val fourNamesB = listOf(
        "P J Harvey Wallbanger",
        "Pretoria James H. Wallbanger",
        "Pamela Jamison Harvey Wally",
    )

    @Test
    fun `should calculate full name from aliases & apply to ss authors`() {
//        firstNamesUseCase.resetMapAliasToNames()
    log.info("SsCalculateFirstNamesTest.should calculate full name from aliases & apply to ss authors()  RESET DONE")
        firstNamesUseCase.mapAliasToFullNamesOnSsAuthorTable(200000)
    }

    @Test
    fun `should get longest middle names combined`() {
        aliasToLongestMiddleName(fourNames) shouldBe "Jons Harvey"
        aliasToLongestMiddleName(fourNamesB) shouldBe "Jamison Harvey"
    }


    @Test
    fun `should get longest last name from aliases`() {
        aliasToLongestLastName(aliases) shouldBe "Pernetti"
        aliasToLongestLastName(jCook) shouldBe "Cook"

        aliasToLongestMiddleName(jCook) shouldBe "J."
        aliasToLongestMiddleName(aliases) shouldBe "Kippington"
    }

    @Test
    fun `should choose longest first name from aliases`() {
        val res = aliasesToLongestFirstName(aliases)
        res shouldBe "Roberta"
        aliasesToLongestFirstName(jCook) shouldBe "Chris"
    }

    //    @Test
    fun `should reset map alias to names`() {
        firstNamesUseCase.resetMapAliasToNames()
    }


    @Test
    fun `should convert aliases to full names on SS Authors`() {
        val res = StringUtils.aliasesToName(aliases)
        res shouldBe aliases[1]
    }

}
