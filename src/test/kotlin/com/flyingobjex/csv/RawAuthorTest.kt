package com.flyingobjex.csv

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.PaperMetatdata
import com.flyingobjex.paperlyzer.parser.CSVParser
import io.kotest.matchers.shouldBe
import java.util.logging.Logger
import org.junit.Test

class RawAuthorTest {

    val paperMetaData = PaperMetatdata(
        "line.shortTitle",
        "line.year",
        "line.title",
        "line.journal",
        "line.orcid",
        emptyList(),
        "line.doi",
        emptyList(),
        "emptyList<Author>()",
    )

    val log: Logger = Logger.getAnonymousLogger()
    private val mongo = Mongo(false)
    private val samplePath = "../tbl_cli_sample.tsv"
    private val livePath = "../tbl_cli_full.tsv"

    val a = "Clark, Jennifer S./Poore, Alistair G. B./Doblin, Martina A."
    val b = "Seppala, Sini/Henriques, Sergio/Draney, Michael L./Foord, Stefan/Gibbons, Alastair T./Gomez, Luz A./Kariko, Sarah/Malumbres-Olarte, Jagoba/Milne, Marc/Vink, Cor J./Cardoso, Pedro"
    val c = "v. Jhering, Hermann"

    @Test
    fun `should match names from author line`(){
        val authors = CSVParser.authorsCellToAuthors(a, paperMetaData)
        authors[0].firstName shouldBe "Jennifer"
        authors[0].middleName shouldBe "S."
        authors[0].lastName shouldBe "Clark"

        authors[1].firstName shouldBe "Alistair"
        authors[1].middleName shouldBe "G. B."
        authors[1].lastName shouldBe "Poore"

        authors[2].firstName shouldBe "Martina"
        authors[2].middleName shouldBe "A."
        authors[2].lastName shouldBe "Doblin"

//        authors[].firstName shouldBe ""
//        authors[].middleName shouldBe ""
//        authors[].lastName shouldBe ""
//
//        authors[].firstName shouldBe ""
//        authors[].middleName shouldBe ""
//        authors[].lastName shouldBe ""
    }

    @Test
    fun `should parse middle initial with last name`(){
        val authors = CSVParser.authorsCellToAuthors(c, paperMetaData)
        val author = authors[0]
        author.lastName shouldBe "Jhering"
        author.middleName shouldBe "v."
        author.firstName shouldBe "Hermann"
    }

    @Test
    fun `should parse author line`(){
        val authors = CSVParser.authorsCellToAuthors(a, paperMetaData)
        authors.size shouldBe 3

        val bres = CSVParser.authorsCellToAuthors(b, paperMetaData)
        bres.size shouldBe 11

       val cres = CSVParser.authorsCellToAuthors(c, paperMetaData)
        cres.size shouldBe 1
    }

    @Test
    fun `should get orcids from orcid line`(){

    }
}
