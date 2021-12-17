package com.flyingobjex.csv

import com.flyingobjex.paperlyzer.Mongo
import com.flyingobjex.paperlyzer.entity.PaperMetatdata
import com.flyingobjex.paperlyzer.parser.CSVParser
import com.flyingobjex.paperlyzer.parser.CSVParser.bylineToNames
import com.flyingobjex.paperlyzer.parser.CSVParser.getFirstName
import com.flyingobjex.paperlyzer.parser.CSVParser.getLastName
import com.flyingobjex.paperlyzer.parser.CSVParser.getMiddleNames
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

    val A = "Clark, Jennifer S./Poore, Alistair G. B./Doblin, Martina A."
    val B = "Seppala, Sini/Henriques, Sergio/Draney, Michael L./Foord, Stefan/Gibbons, Alastair T./Gomez, Luz A./Kariko, Sarah/Malumbres-Olarte, Jagoba/Milne, Marc/" +
        "Vink, Cor J./Cardoso, Pedro"
    val C = "v. Jhering, Hermann"

    val D = "Ricklefs, RE/Buffetaut, E/Hallam, A/Hsu, K/Jablonski, D/Kauffman, EG/Legendre, S/Martin, P/Mclaren, DJ/Myers, N/Traverse, A"

    val E = "Abouali, Mohammad/Nejadhashemi, A. Pouyan/Daneshvar, Fariborz/Herman, Matthew R./Adhikari, Umesh/Calappi, Timothy J./Selegean, James P."
    @Test
    fun `should parse full byline`(){
        val names = bylineToNames(D)
        names.size shouldBe 11
    }

    @Test
    fun `should match names from author line D`(){
        val authors = CSVParser.authorsCellToAuthors(D, paperMetaData)
        authors.size shouldBe 11
        authors[10].firstName shouldBe "A"
        authors[10].middleName shouldBe null
        authors[10].lastName shouldBe "Traverse"

        authors[5].firstName shouldBe "EG"
        authors[5].middleName shouldBe null
        authors[5].lastName shouldBe "Kauffman"
    }

    @Test
    fun `should match names from author line B`(){
        val authors = CSVParser.authorsCellToAuthors(B, paperMetaData)
        authors[0].firstName shouldBe "Sini"
        authors[0].middleName shouldBe null
        authors[0].lastName shouldBe "Seppala"

        authors[1].firstName shouldBe "Sergio"
        authors[1].middleName shouldBe null
        authors[1].lastName shouldBe "Henriques"

        authors[2].firstName shouldBe "Michael"
        authors[2].middleName shouldBe "L."
        authors[2].lastName shouldBe "Draney"

        authors[4].firstName shouldBe "Alastair"
        authors[4].middleName shouldBe "T."
        authors[4].lastName shouldBe "Gibbons"

        authors[9].firstName shouldBe "Cor"
        authors[9].middleName shouldBe "J."
        authors[9].lastName shouldBe "Vink"
    }

    @Test
    fun `should match names from author line`(){
        val authors = CSVParser.authorsCellToAuthors(A, paperMetaData)
        authors[0].firstName shouldBe "Jennifer"
        authors[0].middleName shouldBe "S."
        authors[0].lastName shouldBe "Clark"

        authors[1].firstName shouldBe "Alistair"
        authors[1].middleName shouldBe "G. B."
        authors[1].lastName shouldBe "Poore"

        authors[2].firstName shouldBe "Martina"
        authors[2].middleName shouldBe "A."
        authors[2].lastName shouldBe "Doblin"
    }

    @Test
    fun `should parse middle initial with last name`(){
        val authors = CSVParser.authorsCellToAuthors(C, paperMetaData)
        val author = authors[0]
        author.lastName shouldBe "v. Jhering"
        author.middleName shouldBe null
        author.firstName shouldBe "Hermann"
    }

    @Test
    fun `should parse author line`(){
        val authors = CSVParser.authorsCellToAuthors(A, paperMetaData)
        authors.size shouldBe 3

        val bres = CSVParser.authorsCellToAuthors(B, paperMetaData)
        bres.size shouldBe 11

       val cres = CSVParser.authorsCellToAuthors(C, paperMetaData)
        cres.size shouldBe 1
    }

}
