package com.flyingobjex.process.ssauthor

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.usecase.FirstNamesUseCase
import com.flyingobjex.paperlyzer.util.StringUtils
import com.flyingobjex.paperlyzer.util.StringUtils.aliasToLongestLastName
import com.flyingobjex.paperlyzer.util.StringUtils.aliasesToLongestFirstName
import io.kotest.matchers.shouldBe
import org.junit.Test

class SsCalculateFirstNamesTest {

    val mongo = Mongo()
    val firstNamesUseCase = FirstNamesUseCase(mongo)

    val aliases = listOf(
        "R P",
        "Roberta Pernetti",
        "R. Pernetti",
        "Rob K. P.",
    )

    val jCook = listOf(
        "C J Cook",
        "C. J. Cook",
        "Chris Cook",
    )

    //    @Test
    fun `should calculate full name from aliases & apply to ss authors`() {
        firstNamesUseCase.mapAliasToFullNamesOnSsAuthorTable(100000)
    }

    @Test
    fun `should get longest last name from aliases`(){
        aliasToLongestLastName(aliases) shouldBe "Pernetti"
        aliasToLongestLastName(jCook) shouldBe "Cook"
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
