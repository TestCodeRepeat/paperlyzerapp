package com.flyingobjex

import com.flyingobjex.paperlyzer.parser.CSVParser
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class ParseCSVTest {

    private val samplePath = "../tbl_cli_sample.tsv"
    private val file = File(samplePath)

    @Test
    fun `coordinator should parse & save raw csv paper`(){

    }

//    @Test
    fun `should convert csv line to raw paper`(){
        val rows = CSVParser.csvFileToRawPapers(samplePath)
        assertEquals(rows.size, 27129)
    }

}