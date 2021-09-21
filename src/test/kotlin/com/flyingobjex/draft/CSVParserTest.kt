package com.flyingobjex.draft

import com.flyingobjex.paperlyzer.*
import com.flyingobjex.paperlyzer.entity.PaperMetatdata
import com.flyingobjex.paperlyzer.entity.isAllCaps
import com.flyingobjex.paperlyzer.parser.CSVParser
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CSVParserTest {

    private val path = "src/test/resources/author_table_test.csv"
    private val file = File(path)

    private val rawAthorsCell = "Liu, Min/Gurr, Paul A./Fu, Qiang/Webley, Paul A./Qiao, Greg G."
    private val mongo = Mongo()

//    @Test
    fun `should save author to database`(){
        val res = CSVParser.authorsCellToAuthors(rawAthorsCell, emptyMetaData)
        val author = res[0]
        mongo.addRawAuthor(author)
    }

    @Test
    fun `should check if all letters are uppercase`(){
        assertTrue(isAllCaps("ABC"))
        assertTrue(isAllCaps("AB L"))
        assertTrue(isAllCaps("AB. L."))
        assertFalse(isAllCaps("ABc"))
        assertFalse(isAllCaps("AB c 148."))
        assertTrue(isAllCaps("AB A 148."))
    }

    @Test
    fun `should convert csv line to paper, author & subjects`(){
        val rows = CSVParser.csvFileToRows(path)
        val csvLines = CSVParser.csvRowToRawPapers(rows)
        val res = csvLines.map { CSVParser.rawCsvLinePaperToPaper(it) }
        assertEquals(res.size, 49)
    }

    @Test
    fun `should convert csv line to authors`(){
        val res = CSVParser.authorsCellToAuthors(rawAthorsCell, emptyMetaData)
        assertEquals(res.count(), 5)
    }

    @Test
    fun `should load a csv file`() {
        println("CSVParserTest.()  ")
        println(file.absolutePath)

        val rows = CSVParser.csvFileToRows(path)
        val res = CSVParser.csvRowToRawPapers(rows)

        assertTrue(res.size > 39)
        assertTrue(file.absolutePath.endsWith(".csv"))
    }
}

private val emptyMetaData = PaperMetatdata(
    "",
    "",
    "",
    "",
    "",
    emptyList(),
    "",
    listOf(""),
    "",
)
