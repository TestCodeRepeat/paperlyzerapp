package com.flyingobjex.process.ssauthor

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.usecase.FirstNamesUseCase
import com.flyingobjex.paperlyzer.util.StringUtils
import io.kotest.matchers.shouldBe
import org.junit.Test

class SsCalculateFirstNamesTest {

    val mongo = Mongo()
    val firstNamesUseCase = FirstNamesUseCase(mongo)

    val aliases = listOf(
        "R Pernetti",
        "R. Pernetti",
        "Roberta Pernetti"
    )

    @Test
    fun `should calculate full name from aliases & apply to ss authors`(){

    }

    @Test
    fun `should reset map alias to names`(){
        firstNamesUseCase.resetMapAliasToNames()
    }


    @Test
    fun `should reset calculated first names from aliases on SS Authors`(){
        val res = StringUtils.aliasesToName(aliases)
        res shouldBe aliases[2]
    }

}
