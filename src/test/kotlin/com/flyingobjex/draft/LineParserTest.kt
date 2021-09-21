package com.flyingobjex.draft

import com.flyingobjex.paperlyzer.parser.LineParser.hasKey
import com.flyingobjex.paperlyzer.parser.LineParser.stringToLines
import com.flyingobjex.paperlyzer.parser.LineParser.regexHasAllCaps
import com.flyingobjex.paperlyzer.parser.LineParser.toBody
import com.flyingobjex.paperlyzer.parser.LineParser.toKey
import data.MockData
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LineParserTest {

    @Test
    fun `should split text into header blocks`(){
        val res = stringToLines(MockData.file1snippet)
        assertTrue(res.size > 0)
    }

    @Test
    fun `should extract key from line`(){
        val res = toKey("PU NATURE PUBLISHING GROUP LINE")
        assertEquals(res, "PU")
    }

    @Test
    fun `should split raw line into body for empty lines`(){
        val res = toBody("")
        assertEquals("", res)
    }

    @Test
    fun `should split raw line into body for small lines`(){
        val res = toBody("y")
        assertEquals("y", res)
    }

    @Test
    fun `should split raw line into body`(){
        val res = toBody("AA this should be the body")
        assertEquals("this should be the body", res)
    }

    @Test
    fun `check if line has key`() {
        assertTrue(hasKey("AA  "))
        assertTrue(hasKey("AA  lots of other things here"))
    }

    @Test
    fun `has capital letters in line`() {
        assertTrue(regexHasAllCaps.matches("AA"))
        assertTrue(regexHasAllCaps.matches("BB"))
        assertFalse(regexHasAllCaps.matches("Cb"))
    }

    @Test
    fun `contains spaces`() {
        val res = "AA  ".contains("  ")
        assertTrue(res)

    }
}