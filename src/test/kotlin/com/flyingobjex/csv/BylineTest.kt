package com.flyingobjex.csv

import com.flyingobjex.paperlyzer.parser.CSVParser
import io.kotest.matchers.shouldBe
import org.junit.Test

class BylineTest {

    val D = "Ricklefs, RE/Buffetaut, E/Hallam, A/Hsu, K/Jablonski, D/Kauffman, EG/Legendre, S/Martin, P/Mclaren, DJ/Myers, N/Traverse, A"

    val fullByline = "Zhao Qiang/Wang Jinkuan/Han Yinghua/Zhao, Q"

    @Test
    fun `should parse full byline`(){
        val names = CSVParser.bylineToNames(D)
        names.size shouldBe 11
    }

    @Test
    fun `should convert full byline to names`(){
        val res = CSVParser.bylineToNames(fullByline)
        res.size shouldBe 4

        res[0].firstName shouldBe "Zhao"
        res[1].firstName shouldBe "Wang"
        res[2].firstName shouldBe "Han"
        res[3].firstName shouldBe "Q"

        res[0].lastName shouldBe "Qiang"
        res[1].lastName shouldBe "Jinkuan"
        res[2].lastName shouldBe "Yinghua"
        res[3].lastName shouldBe "Zhao"
    }

    @Test
    fun `should get middle names from byline`(){
        CSVParser.getMiddleNames("Vink, Cor J.") shouldBe "J."
        CSVParser.getMiddleNames("Vink, Corriander James Hatfield") shouldBe "James Hatfield"
    }

    @Test
    fun `should get last name from bylines`(){
        CSVParser.getLastName("Ricklefs, RE") shouldBe "Ricklefs"
        CSVParser.getLastName("Legendre, S A") shouldBe "Legendre"
        CSVParser.getLastName("Draney, Michael L.") shouldBe "Draney"
    }

    @Test
    fun `should get first name from bylines`(){
        CSVParser.getFirstName("Ricklefs, RE") shouldBe "RE"
        CSVParser.getFirstName("Legendre, S A") shouldBe "S"
        CSVParser.getFirstName("Michael") shouldBe "Michael"
    }

}
